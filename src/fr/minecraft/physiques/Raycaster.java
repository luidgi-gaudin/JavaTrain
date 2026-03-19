package fr.minecraft.physiques;

import fr.minecraft.blocs.TypeBloc;
import fr.minecraft.monde.Monde;
import org.joml.Vector3f;
import org.joml.Vector3i;

/**
 * Algorithme DDA Amanatides-Woo pour le lancer de rayon dans la grille.
 */
public class Raycaster {

    public static RaycastResultat lancer(Vector3f origine, Vector3f direction, Monde monde, float portee) {
        Vector3f dir = new Vector3f(direction).normalize();

        // Position de départ en entier (bloc courant)
        int bx = (int) Math.floor(origine.x);
        int by = (int) Math.floor(origine.y);
        int bz = (int) Math.floor(origine.z);

        int stepX = dir.x > 0 ? 1 : dir.x < 0 ? -1 : 0;
        int stepY = dir.y > 0 ? 1 : dir.y < 0 ? -1 : 0;
        int stepZ = dir.z > 0 ? 1 : dir.z < 0 ? -1 : 0;

        float tDeltaX = stepX != 0 ? Math.abs(1.0f / dir.x) : Float.MAX_VALUE;
        float tDeltaY = stepY != 0 ? Math.abs(1.0f / dir.y) : Float.MAX_VALUE;
        float tDeltaZ = stepZ != 0 ? Math.abs(1.0f / dir.z) : Float.MAX_VALUE;

        float tMaxX = stepX != 0 ? ((stepX > 0 ? (bx+1-origine.x) : (origine.x-bx)) / Math.abs(dir.x)) : Float.MAX_VALUE;
        float tMaxY = stepY != 0 ? ((stepY > 0 ? (by+1-origine.y) : (origine.y-by)) / Math.abs(dir.y)) : Float.MAX_VALUE;
        float tMaxZ = stepZ != 0 ? ((stepZ > 0 ? (bz+1-origine.z) : (origine.z-bz)) / Math.abs(dir.z)) : Float.MAX_VALUE;

        int faceX = 0, faceY = 0, faceZ = 0;

        while (true) {
            TypeBloc bloc = monde.getBloc(bx, by, bz);
            if (bloc.estSolide()) {
                return new RaycastResultat(
                    new Vector3i(bx, by, bz),
                    new Vector3i(faceX, faceY, faceZ),
                    bloc
                );
            }

            // Avancer sur l'axe avec le tMax minimal
            if (tMaxX < tMaxY && tMaxX < tMaxZ) {
                if (tMaxX > portee) break;
                bx += stepX; tMaxX += tDeltaX; faceX = -stepX; faceY = 0; faceZ = 0;
            } else if (tMaxY < tMaxZ) {
                if (tMaxY > portee) break;
                by += stepY; tMaxY += tDeltaY; faceX = 0; faceY = -stepY; faceZ = 0;
            } else {
                if (tMaxZ > portee) break;
                bz += stepZ; tMaxZ += tDeltaZ; faceX = 0; faceY = 0; faceZ = -stepZ;
            }
        }
        return null;
    }
}
