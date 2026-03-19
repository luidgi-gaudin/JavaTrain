package fr.minecraft.entites.ia;

import fr.minecraft.entites.Entite;
import org.joml.Vector3f;

import java.util.Random;

public class IaErrance {

    private final Random rng = new Random();
    private float timerMouvement = 0;
    private float timerPause = 0;
    private boolean enPause = false;
    private float dirX, dirZ;

    public void mettreAJour(Entite entite, float dt, float vitesse) {
        if (enPause) {
            timerPause -= dt;
            entite.getPosition(); // no-op, just keep reference
            entite.vitesse.x *= 0.8f;
            entite.vitesse.z *= 0.8f;
            if (timerPause <= 0) {
                enPause = false;
                timerMouvement = 3 + rng.nextFloat() * 5;
                double angle = rng.nextDouble() * Math.PI * 2;
                dirX = (float) Math.cos(angle);
                dirZ = (float) Math.sin(angle);
            }
        } else {
            timerMouvement -= dt;
            entite.vitesse.x = dirX * vitesse;
            entite.vitesse.z = dirZ * vitesse;
            // Orienter yaw vers direction de mouvement
            if (Math.abs(dirX) + Math.abs(dirZ) > 0.01f)
                entite.yaw = (float) Math.toDegrees(Math.atan2(dirZ, dirX));

            if (timerMouvement <= 0) {
                enPause = true;
                timerPause = 2 + rng.nextFloat() * 3;
            }
        }
    }
}
