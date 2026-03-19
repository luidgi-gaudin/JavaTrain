package fr.minecraft.blocs;

public enum TypeBloc {
    AIR        (0.00f, 0.00f, 0.00f, false, true,  0.0f),
    HERBE      (0.40f, 0.75f, 0.20f, true,  false, 0.5f),
    TERRE      (0.55f, 0.35f, 0.15f, true,  false, 0.5f),
    PIERRE     (0.50f, 0.50f, 0.50f, true,  false, 1.5f),
    SABLE      (0.90f, 0.85f, 0.60f, true,  false, 0.5f),
    BOIS       (0.45f, 0.30f, 0.10f, true,  false, 1.0f),
    FEUILLES   (0.20f, 0.60f, 0.10f, true,  true,  0.2f),
    MINERAI_FER(0.50f, 0.50f, 0.55f, true,  false, 3.0f),
    OBSIDIENNE (0.05f, 0.03f, 0.10f, true,  false, 10.0f),
    EAU        (0.10f, 0.30f, 0.80f, false, true,  0.0f),
    BEDROCK    (0.15f, 0.15f, 0.15f, true,  false, Float.MAX_VALUE);

    public final float r, g, b;
    public final boolean estSolide;
    public final boolean estTransparent;
    public final float tempsMinage;

    TypeBloc(float r, float g, float b, boolean estSolide, boolean estTransparent, float tempsMinage) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.estSolide = estSolide;
        this.estTransparent = estTransparent;
        this.tempsMinage = tempsMinage;
    }

    public boolean estSolide()      { return estSolide; }
    public boolean estTransparent() { return estTransparent; }
}
