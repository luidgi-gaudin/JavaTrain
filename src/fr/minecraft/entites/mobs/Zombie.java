package fr.minecraft.entites.mobs;

import fr.minecraft.blocs.TypeBloc;
import fr.minecraft.entites.EntiteItem;
import fr.minecraft.entites.EntiteVivante;
import fr.minecraft.entites.ia.IaErrance;
import fr.minecraft.entites.ia.IaPoursuite;
import fr.minecraft.joueur.Joueur;
import fr.minecraft.monde.Monde;
import org.joml.Vector3f;

import java.util.Random;

public class Zombie extends EntiteVivante {

    private static final float DIST_DETECTION = 16f;
    private static final float DIST_ATTAQUE   = 1.5f;
    private static final float DEGATS_ATTAQUE = 1f;
    private static final float CD_ATTAQUE     = 0.5f;
    private static final float VITESSE        = 2.5f;

    private final IaErrance  iaErrance  = new IaErrance();
    private final IaPoursuite iaPoursuite = new IaPoursuite();
    private final Random rng = new Random();
    private float timerAttaque = 0;
    private Joueur joueurRef;

    public Zombie(float x, float y, float z) {
        super(x, y, z, 0.6f, 1.8f, 20f);
    }

    public void setJoueur(Joueur j) { this.joueurRef = j; }

    @Override
    public void mettreAJour(float dt, Monde monde) {
        super.mettreAJour(dt, monde);
        if (mort) return;

        timerAttaque = Math.max(0, timerAttaque - dt);

        if (joueurRef != null) {
            Vector3f jPos = joueurRef.getPosition();
            float dist = position.distance(jPos);

            if (dist < DIST_DETECTION) {
                // Poursuivre
                iaPoursuite.mettreAJour(this, dt, monde, jPos, VITESSE);
                if (dist < DIST_ATTAQUE && timerAttaque <= 0) {
                    joueurRef.infliger(DEGATS_ATTAQUE);
                    timerAttaque = CD_ATTAQUE;
                }
            } else {
                iaErrance.mettreAJour(this, dt, VITESSE * 0.5f);
            }
        } else {
            iaErrance.mettreAJour(this, dt, VITESSE * 0.5f);
        }

        appliquerPhysiques(dt, monde);
    }

    @Override
    protected void surMort() {
        // drops gérés par le monde (pas implémentés ici)
    }
}
