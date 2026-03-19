import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Un chunk est une portion du monde : grille 3D de blocs.
 * Il gère aussi la construction du mesh OpenGL (VAO/VBO).
 *
 * Chaque sommet contient : position (vec3) + couleur (vec3) + normale (vec3) = 9 floats.
 * Seules les faces adjacentes à l'air sont générées (face culling logique).
 */
public class Chunk {
    public static final int LARGEUR   = 16;
    public static final int HAUTEUR   = 64;
    public static final int PROFONDEUR = 16;

    private final TypeBloc[][][] blocs;
    private int vaoId, vboId;
    private int nombreSommets;

    public Chunk() {
        blocs = new TypeBloc[LARGEUR][HAUTEUR][PROFONDEUR];
        for (int x = 0; x < LARGEUR; x++)
            for (int y = 0; y < HAUTEUR; y++)
                for (int z = 0; z < PROFONDEUR; z++)
                    blocs[x][y][z] = TypeBloc.AIR;
    }

    public TypeBloc getBloc(int x, int y, int z) {
        if (x < 0 || x >= LARGEUR || y < 0 || y >= HAUTEUR || z < 0 || z >= PROFONDEUR)
            return TypeBloc.AIR;
        return blocs[x][y][z];
    }

    public void setBloc(int x, int y, int z, TypeBloc type) {
        if (x < 0 || x >= LARGEUR || y < 0 || y >= HAUTEUR || z < 0 || z >= PROFONDEUR) return;
        blocs[x][y][z] = type;
    }

    /** Construit le mesh et l'envoie sur le GPU. */
    public void construireMaillage() {
        List<Float> sommets = new ArrayList<>();

        for (int x = 0; x < LARGEUR; x++) {
            for (int y = 0; y < HAUTEUR; y++) {
                for (int z = 0; z < PROFONDEUR; z++) {
                    TypeBloc bloc = blocs[x][y][z];
                    if (!bloc.estSolide()) continue;

                    if (!getBloc(x, y + 1, z).estSolide()) ajouterFaceHaut  (sommets, x, y, z, bloc);
                    if (!getBloc(x, y - 1, z).estSolide()) ajouterFaceBas   (sommets, x, y, z, bloc);
                    if (!getBloc(x, y, z - 1).estSolide()) ajouterFaceNord  (sommets, x, y, z, bloc);
                    if (!getBloc(x, y, z + 1).estSolide()) ajouterFaceSud   (sommets, x, y, z, bloc);
                    if (!getBloc(x + 1, y, z).estSolide()) ajouterFaceEst   (sommets, x, y, z, bloc);
                    if (!getBloc(x - 1, y, z).estSolide()) ajouterFaceOuest (sommets, x, y, z, bloc);
                }
            }
        }

        nombreSommets = sommets.size() / 9; // 9 floats par sommet

        FloatBuffer buffer = MemoryUtil.memAllocFloat(sommets.size());
        for (float f : sommets) buffer.put(f);
        buffer.flip();

        vaoId = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoId);

        vboId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

        MemoryUtil.memFree(buffer);

        int stride = 9 * Float.BYTES; // 36 octets par sommet
        // attribute 0 : position (offset 0)
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, stride, 0);
        GL20.glEnableVertexAttribArray(0);
        // attribute 1 : couleur (offset 12)
        GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, stride, 12);
        GL20.glEnableVertexAttribArray(1);
        // attribute 2 : normale (offset 24)
        GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, stride, 24);
        GL20.glEnableVertexAttribArray(2);

        GL30.glBindVertexArray(0);
    }

    public void rendu() {
        if (nombreSommets == 0) return;
        GL30.glBindVertexArray(vaoId);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, nombreSommets);
        GL30.glBindVertexArray(0);
    }

    public void supprimer() {
        GL15.glDeleteBuffers(vboId);
        GL30.glDeleteVertexArrays(vaoId);
    }

    // -------------------------------------------------------------------------
    // Couleur : l'herbe a la face du dessus verte, les côtés couleur terre
    // -------------------------------------------------------------------------
    private float[] couleur(TypeBloc bloc, boolean faceHaute) {
        if (bloc == TypeBloc.HERBE && faceHaute)
            return new float[]{0.4f, 0.75f, 0.2f};
        if (bloc == TypeBloc.HERBE)
            return new float[]{TypeBloc.TERRE.r, TypeBloc.TERRE.g, TypeBloc.TERRE.b};
        return new float[]{bloc.r, bloc.g, bloc.b};
    }

    private void s(List<Float> lst,
                   float x, float y, float z,
                   float r, float g, float b,
                   float nx, float ny, float nz) {
        lst.add(x);  lst.add(y);  lst.add(z);
        lst.add(r);  lst.add(g);  lst.add(b);
        lst.add(nx); lst.add(ny); lst.add(nz);
    }

    // -------------------------------------------------------------------------
    // Génération des faces — ordre CCW (winding côté extérieur = face avant)
    // Vérifié avec la règle de la main droite : cross(edge1, edge2) == normale
    // -------------------------------------------------------------------------

    /** Face du dessus (normale +Y) */
    private void ajouterFaceHaut(List<Float> l, int x, int y, int z, TypeBloc b) {
        float[] c = couleur(b, true);
        float r = c[0], g = c[1], v = c[2];
        s(l, x,   y+1, z,   r,g,v,  0,1,0);
        s(l, x,   y+1, z+1, r,g,v,  0,1,0);
        s(l, x+1, y+1, z+1, r,g,v,  0,1,0);
        s(l, x,   y+1, z,   r,g,v,  0,1,0);
        s(l, x+1, y+1, z+1, r,g,v,  0,1,0);
        s(l, x+1, y+1, z,   r,g,v,  0,1,0);
    }

    /** Face du dessous (normale -Y) */
    private void ajouterFaceBas(List<Float> l, int x, int y, int z, TypeBloc b) {
        float[] c = couleur(b, false);
        float r = c[0], g = c[1], v = c[2];
        s(l, x,   y, z+1, r,g,v,  0,-1,0);
        s(l, x,   y, z,   r,g,v,  0,-1,0);
        s(l, x+1, y, z,   r,g,v,  0,-1,0);
        s(l, x,   y, z+1, r,g,v,  0,-1,0);
        s(l, x+1, y, z,   r,g,v,  0,-1,0);
        s(l, x+1, y, z+1, r,g,v,  0,-1,0);
    }

    /** Face nord (z=0, normale -Z) */
    private void ajouterFaceNord(List<Float> l, int x, int y, int z, TypeBloc b) {
        float[] c = couleur(b, false);
        float r = c[0], g = c[1], v = c[2];
        s(l, x+1, y,   z, r,g,v,  0,0,-1);
        s(l, x,   y,   z, r,g,v,  0,0,-1);
        s(l, x,   y+1, z, r,g,v,  0,0,-1);
        s(l, x+1, y,   z, r,g,v,  0,0,-1);
        s(l, x,   y+1, z, r,g,v,  0,0,-1);
        s(l, x+1, y+1, z, r,g,v,  0,0,-1);
    }

    /** Face sud (z+1, normale +Z) */
    private void ajouterFaceSud(List<Float> l, int x, int y, int z, TypeBloc b) {
        float[] c = couleur(b, false);
        float r = c[0], g = c[1], v = c[2];
        s(l, x,   y,   z+1, r,g,v,  0,0,1);
        s(l, x+1, y,   z+1, r,g,v,  0,0,1);
        s(l, x+1, y+1, z+1, r,g,v,  0,0,1);
        s(l, x,   y,   z+1, r,g,v,  0,0,1);
        s(l, x+1, y+1, z+1, r,g,v,  0,0,1);
        s(l, x,   y+1, z+1, r,g,v,  0,0,1);
    }

    /** Face est (x+1, normale +X) */
    private void ajouterFaceEst(List<Float> l, int x, int y, int z, TypeBloc b) {
        float[] c = couleur(b, false);
        float r = c[0], g = c[1], v = c[2];
        s(l, x+1, y,   z+1, r,g,v,  1,0,0);
        s(l, x+1, y,   z,   r,g,v,  1,0,0);
        s(l, x+1, y+1, z,   r,g,v,  1,0,0);
        s(l, x+1, y,   z+1, r,g,v,  1,0,0);
        s(l, x+1, y+1, z,   r,g,v,  1,0,0);
        s(l, x+1, y+1, z+1, r,g,v,  1,0,0);
    }

    /** Face ouest (x=0, normale -X) */
    private void ajouterFaceOuest(List<Float> l, int x, int y, int z, TypeBloc b) {
        float[] c = couleur(b, false);
        float r = c[0], g = c[1], v = c[2];
        s(l, x, y,   z,   r,g,v,  -1,0,0);
        s(l, x, y,   z+1, r,g,v,  -1,0,0);
        s(l, x, y+1, z+1, r,g,v,  -1,0,0);
        s(l, x, y,   z,   r,g,v,  -1,0,0);
        s(l, x, y+1, z+1, r,g,v,  -1,0,0);
        s(l, x, y+1, z,   r,g,v,  -1,0,0);
    }
}
