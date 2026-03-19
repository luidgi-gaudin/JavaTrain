package fr.minecraft.blocs;

import java.util.HashMap;
import java.util.Map;

/**
 * Mappe TypeBloc × Direction → coordonnée de tuile (col, ligne) dans l'atlas 256×256 (tuiles 16×16).
 * L'atlas contient 16 colonnes × 16 lignes = 256 tuiles.
 */
public class RegistreBlocs {

    private static final Map<Long, int[]> TUILES = new HashMap<>();

    static {
        // Encodage clé : typeOrdinal * 100 + directionOrdinal
        enregistrer(TypeBloc.HERBE,       Direction.HAUT,  0, 0); // herbe haut
        enregistrer(TypeBloc.HERBE,       Direction.BAS,   2, 0); // terre
        enregistrerCotes(TypeBloc.HERBE,                   3, 0); // côtés herbe-terre
        enregistrerTous(TypeBloc.TERRE,                    2, 0);
        enregistrerTous(TypeBloc.PIERRE,                   1, 0);
        enregistrerTous(TypeBloc.SABLE,                    4, 0);
        enregistrerTous(TypeBloc.BOIS,                     5, 0);
        enregistrer(TypeBloc.BOIS,        Direction.HAUT,  6, 0);
        enregistrer(TypeBloc.BOIS,        Direction.BAS,   6, 0);
        enregistrerTous(TypeBloc.FEUILLES,                 7, 0);
        enregistrerTous(TypeBloc.MINERAI_FER,              8, 0);
        enregistrerTous(TypeBloc.OBSIDIENNE,               9, 0);
        enregistrerTous(TypeBloc.EAU,                      10, 0);
        enregistrerTous(TypeBloc.BEDROCK,                  11, 0);
        enregistrerTous(TypeBloc.AIR,                      0, 0);
    }

    private static void enregistrer(TypeBloc bloc, Direction dir, int col, int ligne) {
        TUILES.put(cle(bloc, dir), new int[]{col, ligne});
    }

    private static void enregistrerTous(TypeBloc bloc, int col, int ligne) {
        for (Direction d : Direction.values()) enregistrer(bloc, d, col, ligne);
    }

    private static void enregistrerCotes(TypeBloc bloc, int col, int ligne) {
        enregistrer(bloc, Direction.NORD,  col, ligne);
        enregistrer(bloc, Direction.SUD,   col, ligne);
        enregistrer(bloc, Direction.EST,   col, ligne);
        enregistrer(bloc, Direction.OUEST, col, ligne);
    }

    public static int[] getTuile(TypeBloc bloc, Direction dir) {
        return TUILES.getOrDefault(cle(bloc, dir), new int[]{0, 0});
    }

    private static long cle(TypeBloc bloc, Direction dir) {
        return (long) bloc.ordinal() * 100 + dir.ordinal();
    }

    /**
     * Convertit des coordonnées de tuile en UV (coin supérieur-gauche) pour l'atlas 256×256 (tuiles 16px).
     */
    public static float[] uvPourTuile(int col, int ligne, boolean coinBasGauche) {
        float tileSize = 16.0f / 256.0f;
        float u = col  * tileSize;
        float v = ligne * tileSize;
        return coinBasGauche
            ? new float[]{ u, v + tileSize }  // bas-gauche
            : new float[]{ u, v };             // haut-gauche
    }
}
