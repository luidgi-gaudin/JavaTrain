package fr.minecraft.monde;

/**
 * Bruit de Perlin pur Java.
 * Table de permutation 512, fondu quintique, 12 gradients de cube.
 */
public class BruitPerlin {

    private final int[] p = new int[512];

    private static final int[][] GRADIENTS = {
        {1,1,0},{-1,1,0},{1,-1,0},{-1,-1,0},
        {1,0,1},{-1,0,1},{1,0,-1},{-1,0,-1},
        {0,1,1},{0,-1,1},{0,1,-1},{0,-1,-1}
    };

    public BruitPerlin(long graine) {
        int[] perm = new int[256];
        for (int i = 0; i < 256; i++) perm[i] = i;
        // Mélange Fisher-Yates déterministe
        java.util.Random rng = new java.util.Random(graine);
        for (int i = 255; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = perm[i]; perm[i] = perm[j]; perm[j] = tmp;
        }
        for (int i = 0; i < 512; i++) p[i] = perm[i & 255];
    }

    private float fondu(float t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private float lerp(float t, float a, float b) {
        return a + t * (b - a);
    }

    private float grad(int hash, float x, float y, float z) {
        int[] g = GRADIENTS[hash % 12];
        return g[0] * x + g[1] * y + g[2] * z;
    }

    public float bruit(float x, float y, float z) {
        int X = (int) Math.floor(x) & 255;
        int Y = (int) Math.floor(y) & 255;
        int Z = (int) Math.floor(z) & 255;
        x -= (float) Math.floor(x);
        y -= (float) Math.floor(y);
        z -= (float) Math.floor(z);
        float u = fondu(x), v = fondu(y), w = fondu(z);

        int A  = p[X]+Y, AA = p[A]+Z, AB = p[A+1]+Z;
        int B  = p[X+1]+Y, BA = p[B]+Z, BB = p[B+1]+Z;

        return lerp(w,
            lerp(v,
                lerp(u, grad(p[AA],  x,   y,   z),   grad(p[BA],  x-1, y,   z)),
                lerp(u, grad(p[AB],  x,   y-1, z),   grad(p[BB],  x-1, y-1, z))),
            lerp(v,
                lerp(u, grad(p[AA+1],x,   y,   z-1), grad(p[BA+1],x-1, y,   z-1)),
                lerp(u, grad(p[AB+1],x,   y-1, z-1), grad(p[BB+1],x-1, y-1, z-1))));
    }

    /** Bruit 2D : appel avec y=0 */
    public float bruit2D(float x, float z) {
        return bruit(x, 0, z);
    }

    /** Bruit fractal : somme de n octaves. Retourne [-1, 1]. */
    public float octaves(float x, float z, int n, float persistance) {
        float total = 0, amplitude = 1, frequence = 1, max = 0;
        for (int i = 0; i < n; i++) {
            total    += bruit2D(x * frequence, z * frequence) * amplitude;
            max      += amplitude;
            amplitude *= persistance;
            frequence *= 2;
        }
        return total / max;
    }

    public float octaves3D(float x, float y, float z, int n, float persistance) {
        float total = 0, amplitude = 1, frequence = 1, max = 0;
        for (int i = 0; i < n; i++) {
            total    += bruit(x * frequence, y * frequence, z * frequence) * amplitude;
            max      += amplitude;
            amplitude *= persistance;
            frequence *= 2;
        }
        return total / max;
    }
}
