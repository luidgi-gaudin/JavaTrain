package fr.minecraft.entites.ia;

import fr.minecraft.entites.Entite;
import fr.minecraft.monde.Monde;
import org.joml.Vector3f;
import org.joml.Vector3i;

public class IaPoursuite {

    private float timerRecalcul = 0;
    private float dirX, dirZ;

    public void mettreAJour(Entite entite, float dt, Monde monde, Vector3f cible, float vitesse) {
        timerRecalcul -= dt;
        if (timerRecalcul <= 0) {
            timerRecalcul = 0.5f;
            calcDir(entite.getPosition(), cible);
        }

        entite.vitesse.x = dirX * vitesse;
        entite.vitesse.z = dirZ * vitesse;

        // Saut si bloc devant
        if (entite.auSol) {
            int frontX = (int) Math.floor(entite.getPosition().x + dirX * 0.6f);
            int frontY = (int) Math.floor(entite.getPosition().y + 0.1f);
            int frontZ = (int) Math.floor(entite.getPosition().z + dirZ * 0.6f);
            if (monde.estSolide(frontX, frontY, frontZ))
                entite.vitesse.y = 7f;
        }

        float dx = cible.x - entite.getPosition().x;
        float dz = cible.z - entite.getPosition().z;
        if (Math.abs(dx) + Math.abs(dz) > 0.01f)
            entite.yaw = (float) Math.toDegrees(Math.atan2(dz, dx));
    }

    private void calcDir(Vector3f pos, Vector3f cible) {
        float dx = cible.x - pos.x;
        float dz = cible.z - pos.z;
        float len = (float) Math.sqrt(dx*dx + dz*dz);
        if (len < 0.001f) { dirX = 0; dirZ = 0; return; }
        dirX = dx / len;
        dirZ = dz / len;
    }
}
