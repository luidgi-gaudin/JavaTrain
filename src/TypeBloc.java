public enum TypeBloc {
    AIR    (0.0f, 0.0f, 0.0f),
    HERBE  (0.4f, 0.75f, 0.2f),   // face du dessus — les côtés utilisent la couleur TERRE
    TERRE  (0.55f, 0.35f, 0.15f),
    PIERRE (0.50f, 0.50f, 0.50f),
    SABLE  (0.90f, 0.85f, 0.60f),
    BOIS   (0.45f, 0.30f, 0.10f),
    FEUILLES(0.20f, 0.60f, 0.10f);

    /** Couleur principale du bloc (R, G, B) */
    public final float r, g, b;

    TypeBloc(float r, float g, float b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public boolean estSolide() {
        return this != AIR;
    }
}
