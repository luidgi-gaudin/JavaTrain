package fr.minecraft.joueur;

import fr.minecraft.physiques.AABB;
import org.joml.Vector3f;

public class Joueur {

    public enum Mode { CREATIF, SURVIE }

    // Dimensions hitbox
    public static final float LARGEUR_HITBOX = 0.6f;
    public static final float HAUTEUR_HITBOX = 1.8f;

    private Vector3f position;
    private Vector3f vitesse;
    private float sante;
    private float faim;
    private boolean auSol;
    private Mode mode;
    private final Inventaire inventaire;

    // Survie
    private float timerFaim = 0;
    private float timerRegen = 0;
    private float timerDegats = 0; // invincibilité 0.5s
    private float yMaxAir;
    private boolean enAir;
    private float timerMort = 0;
    private boolean mort = false;

    // Spawn
    private final Vector3f positionSpawn = new Vector3f(8, 70, 8);

    // Vol (créatif)
    private boolean vol = false;

    public Joueur(float x, float y, float z) {
        position  = new Vector3f(x, y, z);
        vitesse   = new Vector3f(0, 0, 0);
        sante     = 20;
        faim      = 20;
        mode      = Mode.CREATIF;
        inventaire = new Inventaire();
    }

    public AABB getAABB() {
        float hw = LARGEUR_HITBOX / 2f;
        return new AABB(
            position.x - hw, position.y, position.z - hw,
            position.x + hw, position.y + HAUTEUR_HITBOX, position.z + hw
        );
    }

    public void infliger(float degats) {
        if (mode == Mode.CREATIF || timerDegats > 0) return;
        sante = Math.max(0, sante - degats);
        timerDegats = 0.5f;
        if (sante <= 0) mourir();
    }

    public void mourir() {
        mort  = true;
        sante = 0;
        timerMort = 0;
    }

    public void respawn() {
        position.set(positionSpawn);
        vitesse.set(0, 0, 0);
        sante = 20;
        faim  = 20;
        mort  = false;
        timerMort = 0;
    }

    public void mettreAJour(float dt) {
        timerDegats = Math.max(0, timerDegats - dt);
        if (mort) { timerMort += dt; return; }

        if (mode == Mode.SURVIE) {
            // Faim
            timerFaim += dt;
            if (timerFaim > 4) {
                timerFaim = 0;
                faim = Math.max(0, faim - (auSol ? 0.025f : 0.005f));
            }
            if (faim <= 0) {
                timerRegen += dt;
                if (timerRegen >= 4) { timerRegen = 0; infliger(0.5f); }
            } else if (faim >= 18 && sante < 20) {
                timerRegen += dt;
                if (timerRegen >= 1) { timerRegen = 0; sante = Math.min(20, sante + 0.5f); }
            } else {
                timerRegen = 0;
            }
            // Dégâts de chute
            if (!auSol) {
                if (enAir) yMaxAir = Math.max(yMaxAir, position.y);
            } else if (enAir) {
                float chute = yMaxAir - position.y - 3;
                if (chute > 0) infliger(chute);
                enAir = false;
            }
            if (!auSol) { if (!enAir) { enAir = true; yMaxAir = position.y; } }
        }
    }

    // Getters/setters
    public Vector3f getPosition()  { return position; }
    public Vector3f getVitesse()   { return vitesse; }
    public boolean isAuSol()       { return auSol; }
    public void setAuSol(boolean v){ auSol = v; }
    public float getSante()        { return sante; }
    public float getFaim()         { return faim; }
    public void setFaim(float f)   { faim = Math.max(0, Math.min(20, f)); }
    public Mode getMode()          { return mode; }
    public void setMode(Mode m)    { mode = m; }
    public boolean isVol()         { return vol; }
    public void setVol(boolean v)  { vol = v; }
    public boolean isMort()        { return mort; }
    public float getTimerMort()    { return timerMort; }
    public Inventaire getInventaire() { return inventaire; }
    public Vector3f getPositionSpawn() { return positionSpawn; }
    public void setPositionSpawn(float x, float y, float z) { positionSpawn.set(x, y, z); }
}
