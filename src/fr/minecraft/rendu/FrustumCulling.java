package fr.minecraft.rendu;

import org.joml.Matrix4f;

/**
 * Frustum culling Gribb-Hartmann.
 * Extraction des 6 plans depuis la matrice VP (view-projection).
 */
public class FrustumCulling {

    private final float[][] plans = new float[6][4]; // [plan][a,b,c,d]

    public void mettreAJour(Matrix4f vp) {
        float[] m = new float[16];
        vp.get(m);
        // Extraction des plans (format colonne-majeur JOML : m[col*4+ligne])
        // Gauche   = row3 + row0
        plans[0][0] = m[3]+m[0]; plans[0][1] = m[7]+m[4]; plans[0][2] = m[11]+m[8];  plans[0][3] = m[15]+m[12];
        // Droite   = row3 - row0
        plans[1][0] = m[3]-m[0]; plans[1][1] = m[7]-m[4]; plans[1][2] = m[11]-m[8];  plans[1][3] = m[15]-m[12];
        // Bas      = row3 + row1
        plans[2][0] = m[3]+m[1]; plans[2][1] = m[7]+m[5]; plans[2][2] = m[11]+m[9];  plans[2][3] = m[15]+m[13];
        // Haut     = row3 - row1
        plans[3][0] = m[3]-m[1]; plans[3][1] = m[7]-m[5]; plans[3][2] = m[11]-m[9];  plans[3][3] = m[15]-m[13];
        // Proche   = row3 + row2
        plans[4][0] = m[3]+m[2]; plans[4][1] = m[7]+m[6]; plans[4][2] = m[11]+m[10]; plans[4][3] = m[15]+m[14];
        // Loin     = row3 - row2
        plans[5][0] = m[3]-m[2]; plans[5][1] = m[7]-m[6]; plans[5][2] = m[11]-m[10]; plans[5][3] = m[15]-m[14];

        // Normaliser
        for (float[] plan : plans) {
            float len = (float) Math.sqrt(plan[0]*plan[0] + plan[1]*plan[1] + plan[2]*plan[2]);
            if (len > 0) { plan[0]/=len; plan[1]/=len; plan[2]/=len; plan[3]/=len; }
        }
    }

    /** Teste si une AABB (min-max) est visible. */
    public boolean estVisible(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        for (float[] p : plans) {
            // Coin P+ (le plus "positif" dans la direction de la normale)
            float px = (p[0] >= 0) ? maxX : minX;
            float py = (p[1] >= 0) ? maxY : minY;
            float pz = (p[2] >= 0) ? maxZ : minZ;
            if (p[0]*px + p[1]*py + p[2]*pz + p[3] < 0) return false;
        }
        return true;
    }
}
