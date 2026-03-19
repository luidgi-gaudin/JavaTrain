package fr.minecraft.physiques;

import fr.minecraft.blocs.TypeBloc;
import org.joml.Vector3f;
import org.joml.Vector3i;

public record RaycastResultat(Vector3i posBloc, Vector3i face, TypeBloc typeBloc) {

    /** Position où poser un bloc adjacent (posBloc + face). */
    public Vector3i posAdjacenteBloc() {
        return new Vector3i(posBloc.x + face.x, posBloc.y + face.y, posBloc.z + face.z);
    }

    /** Centre du bloc touché, pour le rendu du contour. */
    public Vector3f centre() {
        return new Vector3f(posBloc.x + 0.5f, posBloc.y + 0.5f, posBloc.z + 0.5f);
    }
}
