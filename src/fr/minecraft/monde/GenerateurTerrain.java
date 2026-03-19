package fr.minecraft.monde;

import fr.minecraft.blocs.TypeBloc;

import java.util.Random;

public class GenerateurTerrain {

    private static final int NIVEAU_MER = 32;
    private final BruitPerlin bruit;
    private final Random rng;

    public GenerateurTerrain(long graine) {
        bruit = new BruitPerlin(graine);
        rng   = new Random(graine);
    }

    public void generer(Chunk chunk, int cx, int cz) {
        int[][] hauteurs = new int[Chunk.LARGEUR][Chunk.PROFONDEUR];

        // Passe 1 : calculer hauteurs de terrain
        for (int lx = 0; lx < Chunk.LARGEUR; lx++) {
            for (int lz = 0; lz < Chunk.PROFONDEUR; lz++) {
                int mx = cx * Chunk.LARGEUR + lx;
                int mz = cz * Chunk.PROFONDEUR + lz;
                float h = bruit.octaves(mx * 0.01f, mz * 0.01f, 4, 0.5f);
                hauteurs[lx][lz] = NIVEAU_MER + (int)(h * 20);
            }
        }

        // Passe 2 : remplir blocs
        for (int lx = 0; lx < Chunk.LARGEUR; lx++) {
            for (int lz = 0; lz < Chunk.PROFONDEUR; lz++) {
                int mx = cx * Chunk.LARGEUR + lx;
                int mz = cz * Chunk.PROFONDEUR + lz;
                int h  = hauteurs[lx][lz];
                h = Math.max(2, Math.min(Chunk.HAUTEUR - 3, h));

                // Bedrock y=0
                chunk.setBloc(lx, 0, lz, TypeBloc.BEDROCK);
                // Pierre y=1..h-3
                for (int y = 1; y <= h - 3; y++) {
                    // Minerai de fer aléatoire
                    TypeBloc b = (rng.nextFloat() < 0.02f && y < 20) ? TypeBloc.MINERAI_FER : TypeBloc.PIERRE;
                    chunk.setBloc(lx, y, lz, b);
                }
                // Terre h-2..h-1
                if (h - 2 >= 1) chunk.setBloc(lx, h - 2, lz, TypeBloc.TERRE);
                if (h - 1 >= 1) chunk.setBloc(lx, h - 1, lz, TypeBloc.TERRE);
                // Herbe en surface
                chunk.setBloc(lx, h, lz, TypeBloc.HERBE);

                // Cavernes : bruit 3D
                for (int y = 1; y < h - 1; y++) {
                    float cv = bruit.octaves3D(mx * 0.05f, y * 0.05f, mz * 0.05f, 2, 0.5f);
                    if (cv > 0.55f) chunk.setBloc(lx, y, lz, TypeBloc.AIR);
                }

                // Arbres (5% de chance sur herbe)
                if (rng.nextFloat() < 0.005f) {
                    planterArbre(chunk, lx, h + 1, lz);
                }
            }
        }
    }

    private void planterArbre(Chunk chunk, int x, int baseY, int z) {
        int hauteurTronc = 4;
        for (int y = baseY; y < baseY + hauteurTronc && y < Chunk.HAUTEUR; y++)
            chunk.setBloc(x, y, z, TypeBloc.BOIS);

        int centreY = baseY + hauteurTronc;
        int rayon = 2;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -rayon; dx <= rayon; dx++) {
                for (int dz = -rayon; dz <= rayon; dz++) {
                    if (dx*dx + dz*dz <= rayon*rayon) {
                        int bx = x + dx, by = centreY + dy, bz = z + dz;
                        if (bx >= 0 && bx < Chunk.LARGEUR && bz >= 0 && bz < Chunk.PROFONDEUR
                                && by >= 0 && by < Chunk.HAUTEUR
                                && chunk.getBloc(bx, by, bz) == TypeBloc.AIR) {
                            chunk.setBloc(bx, by, bz, TypeBloc.FEUILLES);
                        }
                    }
                }
            }
        }
    }
}
