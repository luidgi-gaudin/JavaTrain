package fr.minecraft.blocs;

public enum Direction {
    HAUT ( 0,  1,  0),
    BAS  ( 0, -1,  0),
    NORD ( 0,  0, -1),
    SUD  ( 0,  0,  1),
    EST  ( 1,  0,  0),
    OUEST(-1,  0,  0);

    public final int dx, dy, dz;

    Direction(int dx, int dy, int dz) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
    }
}
