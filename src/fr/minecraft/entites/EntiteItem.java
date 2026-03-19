package fr.minecraft.entites;

import fr.minecraft.blocs.TypeBloc;
import fr.minecraft.joueur.Joueur;
import fr.minecraft.monde.Monde;
import org.joml.Vector3f;

public class EntiteItem extends Entite {

    private static final float DESPAWN_SECS   = 300f;
    private static final float DIST_ATTRACTION = 1.5f;
    private static final float DIST_PICKUP     = 0.8f;
    private static final float VITESSE_ATTRACTION = 5f;

    private final TypeBloc type;
    private float timerVie = 0;
    private float timerRotation = 0;
    private Joueur joueurRef;

    public EntiteItem(float x, float y, float z, TypeBloc type) {
        super(x, y, z, 0.25f, 0.25f);
        this.type = type;
    }

    public void setJoueur(Joueur j) { this.joueurRef = j; }

    @Override
    public void mettreAJour(float dt, Monde monde) {
        timerVie      += dt;
        timerRotation += dt;
        yaw = timerRotation * 90f; // rotation visible

        if (timerVie >= DESPAWN_SECS) { mort = true; return; }

        appliquerPhysiques(dt, monde);

        if (joueurRef != null) {
            Vector3f jPos = joueurRef.getPosition();
            float dist = position.distance(jPos);
            if (dist < DIST_ATTRACTION) {
                Vector3f dir = new Vector3f(jPos).sub(position).normalize().mul(VITESSE_ATTRACTION);
                vitesse.x = dir.x; vitesse.z = dir.z;
            }
            if (dist < DIST_PICKUP) {
                joueurRef.getInventaire().ajouterBloc(type, 1);
                mort = true;
            }
        }
    }

    public TypeBloc getType() { return type; }
}
