package fr.minecraft.monde;

import fr.minecraft.blocs.Direction;
import fr.minecraft.blocs.RegistreBlocs;
import fr.minecraft.blocs.TypeBloc;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Chunk 16×64×16. Format vertex : position(3) + uv(2) + normale(3) + ao(1) = 9 floats, 36 octets.
 * Layout :
 *   loc 0 = position (offset 0)
 *   loc 1 = uv       (offset 12)
 *   loc 2 = normale  (offset 20)
 *   loc 3 = ao       (offset 32)
 */
public class Chunk {

    public static final int LARGEUR    = 16;
    public static final int HAUTEUR    = 64;
    public static final int PROFONDEUR = 16;

    // Position monde du chunk (en blocs)
    public final int mondeX, mondeZ;

    private final TypeBloc[][][] blocs;
    private int vaoId, vboId;
    private int nombreSommets;
    private boolean dirty = true;

    // Accès cross-chunk pour AO et culling
    private Monde monde;

    public Chunk(int cx, int cz) {
        this.mondeX = cx * LARGEUR;
        this.mondeZ = cz * PROFONDEUR;
        blocs = new TypeBloc[LARGEUR][HAUTEUR][PROFONDEUR];
        for (TypeBloc[][] plan : blocs)
            for (TypeBloc[] col : plan)
                java.util.Arrays.fill(col, TypeBloc.AIR);
    }

    public void setMonde(Monde monde) { this.monde = monde; }

    public TypeBloc getBloc(int x, int y, int z) {
        if (y < 0 || y >= HAUTEUR) return TypeBloc.AIR;
        if (x < 0 || x >= LARGEUR || z < 0 || z >= PROFONDEUR) {
            if (monde != null) return monde.getBloc(mondeX + x, y, mondeZ + z);
            return TypeBloc.AIR;
        }
        return blocs[x][y][z];
    }

    public void setBloc(int x, int y, int z, TypeBloc type) {
        if (x < 0 || x >= LARGEUR || y < 0 || y >= HAUTEUR || z < 0 || z >= PROFONDEUR) return;
        blocs[x][y][z] = type;
        dirty = true;
    }

    public boolean isDirty() { return dirty; }

    public void construireMaillage() {
        List<Float> sommets = new ArrayList<>(4096);

        for (int x = 0; x < LARGEUR; x++) {
            for (int y = 0; y < HAUTEUR; y++) {
                for (int z = 0; z < PROFONDEUR; z++) {
                    TypeBloc bloc = blocs[x][y][z];
                    if (!bloc.estSolide()) continue;

                    if (!getBloc(x, y+1, z).estSolide()) ajouterFace(sommets, x, y, z, bloc, Direction.HAUT);
                    if (!getBloc(x, y-1, z).estSolide()) ajouterFace(sommets, x, y, z, bloc, Direction.BAS);
                    if (!getBloc(x, y, z-1).estSolide()) ajouterFace(sommets, x, y, z, bloc, Direction.NORD);
                    if (!getBloc(x, y, z+1).estSolide()) ajouterFace(sommets, x, y, z, bloc, Direction.SUD);
                    if (!getBloc(x+1, y, z).estSolide()) ajouterFace(sommets, x, y, z, bloc, Direction.EST);
                    if (!getBloc(x-1, y, z).estSolide()) ajouterFace(sommets, x, y, z, bloc, Direction.OUEST);
                }
            }
        }

        dirty = false;
        nombreSommets = sommets.size() / 9;
        if (nombreSommets == 0) { libererGPU(); return; }

        FloatBuffer buf = MemoryUtil.memAllocFloat(sommets.size());
        for (float f : sommets) buf.put(f);
        buf.flip();

        if (vaoId == 0) {
            vaoId = GL30.glGenVertexArrays();
            vboId = GL15.glGenBuffers();
        }

        GL30.glBindVertexArray(vaoId);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buf, GL15.GL_DYNAMIC_DRAW);
        MemoryUtil.memFree(buf);

        int stride = 9 * Float.BYTES;
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, stride, 0);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, stride, 12);
        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, stride, 20);
        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(3, 1, GL11.GL_FLOAT, false, stride, 32);
        GL20.glEnableVertexAttribArray(3);
        GL30.glBindVertexArray(0);
    }

    private void libererGPU() {
        if (vboId != 0) { GL15.glDeleteBuffers(vboId); vboId = 0; }
        if (vaoId != 0) { GL30.glDeleteVertexArrays(vaoId); vaoId = 0; }
    }

    public void rendu() {
        if (nombreSommets == 0 || vaoId == 0) return;
        GL30.glBindVertexArray(vaoId);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, nombreSommets);
        GL30.glBindVertexArray(0);
    }

    public void supprimer() { libererGPU(); }

    // -------------------------------------------------------------------------
    // Génération de face avec UV (atlas) + AO par vertex
    // -------------------------------------------------------------------------
    private void ajouterFace(List<Float> l, int x, int y, int z, TypeBloc bloc, Direction dir) {
        int[] tuile = RegistreBlocs.getTuile(bloc, dir);
        float tileSize = 16.0f / 256.0f;
        float u0 = tuile[0] * tileSize, v0 = tuile[1] * tileSize;
        float u1 = u0 + tileSize,       v1 = v0 + tileSize;

        float wx = mondeX + x, wy = y, wz = mondeZ + z;

        switch (dir) {
            case HAUT -> {
                // Sommets : (x,y+1,z), (x,y+1,z+1), (x+1,y+1,z+1), (x+1,y+1,z)
                float ao0 = ao(x, y+1, z,   0,1,0, -1,0,0,  0,0,-1);
                float ao1 = ao(x, y+1, z+1, 0,1,0, -1,0,0,  0,0, 1);
                float ao2 = ao(x+1,y+1,z+1, 0,1,0,  1,0,0,  0,0, 1);
                float ao3 = ao(x+1,y+1,z,   0,1,0,  1,0,0,  0,0,-1);
                quad(l, wx,wy+1,wz, u0,v1, 0,1,0, ao0,
                        wx,wy+1,wz+1, u0,v0, 0,1,0, ao1,
                        wx+1,wy+1,wz+1, u1,v0, 0,1,0, ao2,
                        wx+1,wy+1,wz, u1,v1, 0,1,0, ao3);
            }
            case BAS -> {
                float ao0 = ao(x, y,   z+1, 0,-1,0, -1,0,0,  0,0, 1);
                float ao1 = ao(x, y,   z,   0,-1,0, -1,0,0,  0,0,-1);
                float ao2 = ao(x+1,y,  z,   0,-1,0,  1,0,0,  0,0,-1);
                float ao3 = ao(x+1,y,  z+1, 0,-1,0,  1,0,0,  0,0, 1);
                quad(l, wx,wy,wz+1, u0,v0, 0,-1,0, ao0,
                        wx,wy,wz, u0,v1, 0,-1,0, ao1,
                        wx+1,wy,wz, u1,v1, 0,-1,0, ao2,
                        wx+1,wy,wz+1, u1,v0, 0,-1,0, ao3);
            }
            case NORD -> {
                float ao0 = ao(x+1,y,  z, 0,0,-1, 1,0,0, 0,-1,0);
                float ao1 = ao(x, y,   z, 0,0,-1,-1,0,0, 0,-1,0);
                float ao2 = ao(x, y+1, z, 0,0,-1,-1,0,0, 0, 1,0);
                float ao3 = ao(x+1,y+1,z, 0,0,-1, 1,0,0, 0, 1,0);
                quad(l, wx+1,wy,wz, u0,v1, 0,0,-1, ao0,
                        wx,wy,wz, u1,v1, 0,0,-1, ao1,
                        wx,wy+1,wz, u1,v0, 0,0,-1, ao2,
                        wx+1,wy+1,wz, u0,v0, 0,0,-1, ao3);
            }
            case SUD -> {
                float ao0 = ao(x, y,   z+1, 0,0,1,-1,0,0, 0,-1,0);
                float ao1 = ao(x+1,y,  z+1, 0,0,1, 1,0,0, 0,-1,0);
                float ao2 = ao(x+1,y+1,z+1, 0,0,1, 1,0,0, 0, 1,0);
                float ao3 = ao(x, y+1, z+1, 0,0,1,-1,0,0, 0, 1,0);
                quad(l, wx,wy,wz+1, u0,v1, 0,0,1, ao0,
                        wx+1,wy,wz+1, u1,v1, 0,0,1, ao1,
                        wx+1,wy+1,wz+1, u1,v0, 0,0,1, ao2,
                        wx,wy+1,wz+1, u0,v0, 0,0,1, ao3);
            }
            case EST -> {
                float ao0 = ao(x+1,y,   z+1, 1,0,0, 0,-1,0, 0,0, 1);
                float ao1 = ao(x+1,y,   z,   1,0,0, 0,-1,0, 0,0,-1);
                float ao2 = ao(x+1,y+1, z,   1,0,0, 0, 1,0, 0,0,-1);
                float ao3 = ao(x+1,y+1, z+1, 1,0,0, 0, 1,0, 0,0, 1);
                quad(l, wx+1,wy,wz+1, u0,v1, 1,0,0, ao0,
                        wx+1,wy,wz, u1,v1, 1,0,0, ao1,
                        wx+1,wy+1,wz, u1,v0, 1,0,0, ao2,
                        wx+1,wy+1,wz+1, u0,v0, 1,0,0, ao3);
            }
            case OUEST -> {
                float ao0 = ao(x,y,   z,   -1,0,0, 0,-1,0, 0,0,-1);
                float ao1 = ao(x,y,   z+1, -1,0,0, 0,-1,0, 0,0, 1);
                float ao2 = ao(x,y+1, z+1, -1,0,0, 0, 1,0, 0,0, 1);
                float ao3 = ao(x,y+1, z,   -1,0,0, 0, 1,0, 0,0,-1);
                quad(l, wx,wy,wz, u1,v1, -1,0,0, ao0,
                        wx,wy,wz+1, u0,v1, -1,0,0, ao1,
                        wx,wy+1,wz+1, u0,v0, -1,0,0, ao2,
                        wx,wy+1,wz, u1,v0, -1,0,0, ao3);
            }
        }
    }

    /**
     * Émet 2 triangles (quad) avec inversion de diagonale si nécessaire pour AO.
     */
    private void quad(List<Float> l,
                      float x0,float y0,float z0, float u0,float v0, float nx,float ny,float nz, float ao0,
                      float x1,float y1,float z1, float u1,float v1, float ao1,
                      float x2,float y2,float z2, float u2,float v2, float ao2,
                      float x3,float y3,float z3, float u3,float v3, float ao3) {
        // Si AO[0]+AO[2] < AO[1]+AO[3] : inverser la diagonale pour éviter l'artefact anisotrope
        if (ao0 + ao2 < ao1 + ao3) {
            // Diagonale 0-1-3 et 1-2-3
            s(l,x0,y0,z0,u0,v0,nx,ny,nz,ao0); s(l,x1,y1,z1,u1,v1,nx,ny,nz,ao1); s(l,x3,y3,z3,u3,v3,nx,ny,nz,ao3);
            s(l,x1,y1,z1,u1,v1,nx,ny,nz,ao1); s(l,x2,y2,z2,u2,v2,nx,ny,nz,ao2); s(l,x3,y3,z3,u3,v3,nx,ny,nz,ao3);
        } else {
            // Diagonale 0-1-2 et 0-2-3
            s(l,x0,y0,z0,u0,v0,nx,ny,nz,ao0); s(l,x1,y1,z1,u1,v1,nx,ny,nz,ao1); s(l,x2,y2,z2,u2,v2,nx,ny,nz,ao2);
            s(l,x0,y0,z0,u0,v0,nx,ny,nz,ao0); s(l,x2,y2,z2,u2,v2,nx,ny,nz,ao2); s(l,x3,y3,z3,u3,v3,nx,ny,nz,ao3);
        }
    }

    private void s(List<Float> l, float x, float y, float z, float u, float v,
                   float nx, float ny, float nz, float ao) {
        l.add(x); l.add(y); l.add(z);
        l.add(u); l.add(v);
        l.add(nx); l.add(ny); l.add(nz);
        l.add(ao);
    }

    /**
     * Calcule l'AO d'un sommet.
     * @param vx,vy,vz : position du sommet dans les coords locales
     * @param nx,ny,nz : normale de la face (±1 sur un axe)
     * @param s1x.. : décalage vers le côté 1
     * @param s2x.. : décalage vers le côté 2 (le coin est s1+s2)
     */
    private float ao(int vx, int vy, int vz,
                     int nx, int ny, int nz,
                     int s1x, int s1y, int s1z,
                     int s2x, int s2y, int s2z) {
        boolean side1  = getBloc(vx+nx+s1x, vy+ny+s1y, vz+nz+s1z).estSolide();
        boolean side2  = getBloc(vx+nx+s2x, vy+ny+s2y, vz+nz+s2z).estSolide();
        boolean corner = getBloc(vx+nx+s1x+s2x, vy+ny+s1y+s2y, vz+nz+s1z+s2z).estSolide();
        int aoInt = (side1 && side2) ? 0 : 3 - (b(side1) + b(side2) + b(corner));
        return aoInt / 3.0f * 0.5f + 0.5f;
    }

    private int b(boolean v) { return v ? 1 : 0; }
}
