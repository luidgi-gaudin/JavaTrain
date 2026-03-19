package fr.minecraft.entites.mobs;

import fr.minecraft.entites.EntiteVivante;
import fr.minecraft.entites.ia.IaErrance;
import fr.minecraft.joueur.Joueur;
import fr.minecraft.monde.Monde;
import org.joml.Vector3f;

public class Vache extends EntiteVivante {

    private static final float DIST_FUITE  = 3f;
    private static final float VITESSE     = 2f;
    private static final float VITESSE_FUITE = 4f;

    private final IaErrance iaErrance = new IaErrance();
    private Joueur joueurRef;
    private boolean fuite = false;

    public Vache(float x, float y, float z) {
        super(x, y, z, 0.9f, 1.4f, 10f);
    }

    public void setJoueur(Joueur j) { this.joueurRef = j; }

    @Override
    public void mettreAJour(float dt, Monde monde) {
        super.mettreAJour(dt, monde);
        if (mort) return;

        fuite = false;
        if (joueurRef != null) {
            float dist = position.distance(joueurRef.getPosition());
            if (dist < DIST_FUITE) {
                fuite = true;
                Vector3f away = new Vector3f(position).sub(joueurRef.getPosition()).normalize().mul(VITESSE_FUITE);
                vitesse.x = away.x;
                vitesse.z = away.z;
                yaw = (float) Math.toDegrees(Math.atan2(away.z, away.x));
            }
        }
        if (!fuite) iaErrance.mettreAJour(this, dt, VITESSE);

        appliquerPhysiques(dt, monde);
    }

    @Override
    protected void surMort() {}
}
