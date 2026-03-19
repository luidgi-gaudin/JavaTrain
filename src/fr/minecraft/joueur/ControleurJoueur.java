package fr.minecraft.joueur;

import fr.minecraft.camera.Camera;
import fr.minecraft.monde.Monde;
import fr.minecraft.physiques.RaycastResultat;
import fr.minecraft.physiques.Raycaster;
import org.joml.Vector3i;
import org.lwjgl.glfw.GLFW;

public class ControleurJoueur {

    private static final float VITESSE_MARCHE = 4.5f;
    private static final float VITESSE_VOL    = 10.0f;

    private final long fenetre;
    private final Joueur joueur;
    private final Camera camera;
    private final PhysiquesJoueur physiques;
    private final Monde monde;

    // Minage
    private float progressionMinage = 0;
    private Vector3i blocEnMinage = null;
    private boolean clicGauchePrec = false;
    private boolean clicDroitPrec  = false;

    // Double-tap space pour vol
    private boolean spacePrec = false;
    private float timerDoubleTap = 0;
    private boolean enAttenteTap = false;

    // F3 debug toggle
    private boolean f3Prec = false;
    private boolean f3Actif = false;

    // Echap / pause
    private boolean escPrec = false;

    public ControleurJoueur(long fenetre, Joueur joueur, Camera camera, PhysiquesJoueur physiques, Monde monde) {
        this.fenetre   = fenetre;
        this.joueur    = joueur;
        this.camera    = camera;
        this.physiques = physiques;
        this.monde     = monde;
    }

    public void mettreAJour(float dt) {
        if (joueur.isMort()) {
            if (GLFW.glfwGetKey(fenetre, GLFW.GLFW_KEY_R) == GLFW.GLFW_PRESS) joueur.respawn();
            return;
        }

        gererMouvement(dt);
        gererInteraction(dt);
        gererToggleMode();

        // Synchroniser la caméra sur la position du joueur (yeux à h=1.62)
        camera.setPosition(
            joueur.getPosition().x,
            joueur.getPosition().y + 1.62f,
            joueur.getPosition().z
        );
    }

    private void gererMouvement(float dt) {
        boolean enVol = joueur.getMode() == Joueur.Mode.CREATIF && joueur.isVol();
        float vitesse = enVol ? VITESSE_VOL : VITESSE_MARCHE;

        // Directions (projetées sur XZ si pas en vol)
        org.joml.Vector3f forward = camera.getDirection();
        if (!enVol) { forward.y = 0; if (forward.lengthSquared() > 0.001f) forward.normalize(); }
        org.joml.Vector3f right = new org.joml.Vector3f(forward).cross(new org.joml.Vector3f(0,1,0)).normalize();

        org.joml.Vector3f v = joueur.getVitesse();
        float targetX = 0, targetZ = 0, targetY = enVol ? 0 : v.y;

        if (GLFW.glfwGetKey(fenetre, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) { targetX += forward.x * vitesse; targetZ += forward.z * vitesse; }
        if (GLFW.glfwGetKey(fenetre, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) { targetX -= forward.x * vitesse; targetZ -= forward.z * vitesse; }
        if (GLFW.glfwGetKey(fenetre, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS) { targetX -= right.x * vitesse; targetZ -= right.z * vitesse; }
        if (GLFW.glfwGetKey(fenetre, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS) { targetX += right.x * vitesse; targetZ += right.z * vitesse; }
        if (enVol && GLFW.glfwGetKey(fenetre, GLFW.GLFW_KEY_SPACE)      == GLFW.GLFW_PRESS) targetY =  vitesse;
        if (enVol && GLFW.glfwGetKey(fenetre, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS) targetY = -vitesse;

        v.x = targetX; v.z = targetZ;
        if (enVol) v.y = targetY;

        // Saut
        boolean space = GLFW.glfwGetKey(fenetre, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS;
        if (!enVol) {
            if (joueur.getMode() == Joueur.Mode.CREATIF) {
                // Double-tap SPACE pour activer le vol
                timerDoubleTap = Math.max(0, timerDoubleTap - dt);
                if (space && !spacePrec) {
                    if (enAttenteTap && timerDoubleTap > 0) {
                        joueur.setVol(true);
                    } else {
                        enAttenteTap = true;
                        timerDoubleTap = 0.3f;
                    }
                }
            }
            if (space && !spacePrec) physiques.appliquerSaut(joueur);
        } else {
            // Espace double-tap pour désactiver vol
            if (!space && spacePrec) { enAttenteTap = false; }
        }
        spacePrec = space;

        physiques.mettreAJour(joueur, dt, monde);
    }

    private void gererInteraction(float dt) {
        RaycastResultat res = Raycaster.lancer(camera.getPosition(), camera.getDirection(), monde, 5.0f);
        this.dernierRaycast = res;

        boolean clicGauche = GLFW.glfwGetMouseButton(fenetre, GLFW.GLFW_MOUSE_BUTTON_LEFT)  == GLFW.GLFW_PRESS;
        boolean clicDroit  = GLFW.glfwGetMouseButton(fenetre, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;

        // Minage
        if (clicGauche && res != null) {
            Vector3i pos = res.posBloc();
            if (blocEnMinage == null || !pos.equals(blocEnMinage)) {
                blocEnMinage = new Vector3i(pos);
                progressionMinage = 0;
            }
            float tempsMinage = joueur.getMode() == Joueur.Mode.CREATIF ? 0 : res.typeBloc().tempsMinage;
            progressionMinage += dt;
            if (progressionMinage >= tempsMinage || joueur.getMode() == Joueur.Mode.CREATIF) {
                monde.setBloc(pos.x, pos.y, pos.z, fr.minecraft.blocs.TypeBloc.AIR);
                if (joueur.getMode() == Joueur.Mode.SURVIE)
                    joueur.getInventaire().ajouterBloc(res.typeBloc(), 1);
                progressionMinage = 0;
                blocEnMinage = null;
                clicGauche = false;
            }
        } else {
            progressionMinage = 0;
            blocEnMinage = null;
        }
        clicGauchePrec = clicGauche;

        // Placement de bloc
        if (clicDroit && !clicDroitPrec && res != null) {
            Vector3i adj = res.posAdjacenteBloc();
            fr.minecraft.blocs.TypeBloc aBloquer = joueur.getInventaire().getBlocActif();
            if (aBloquer != fr.minecraft.blocs.TypeBloc.AIR) {
                // Vérifier non-collision avec joueur
                fr.minecraft.physiques.AABB aabbBloc = new fr.minecraft.physiques.AABB(
                    adj.x, adj.y, adj.z, adj.x+1, adj.y+1, adj.z+1);
                if (!joueur.getAABB().chevauche(aabbBloc)) {
                    monde.setBloc(adj.x, adj.y, adj.z, aBloquer);
                    if (joueur.getMode() == Joueur.Mode.SURVIE)
                        joueur.getInventaire().consommerBlocActif();
                    else
                        joueur.getInventaire().remplirInfini();
                }
            }
        }
        clicDroitPrec = clicDroit;

        // Hotbar touches 1-9
        for (int i = 0; i < 9; i++) {
            if (GLFW.glfwGetKey(fenetre, GLFW.GLFW_KEY_1 + i) == GLFW.GLFW_PRESS)
                joueur.getInventaire().setSlotActif(i);
        }
    }

    private void gererToggleMode() {
        // F pour basculer créatif/survie
        boolean f = GLFW.glfwGetKey(fenetre, GLFW.GLFW_KEY_F) == GLFW.GLFW_PRESS;
        // F3 debug
        boolean f3 = GLFW.glfwGetKey(fenetre, GLFW.GLFW_KEY_F3) == GLFW.GLFW_PRESS;
        if (f3 && !f3Prec) f3Actif = !f3Actif;
        f3Prec = f3;
    }

    // Pour le HUD
    private RaycastResultat dernierRaycast = null;

    public RaycastResultat getDernierRaycast() { return dernierRaycast; }
    public float getProgressionMinage()         { return progressionMinage; }
    public Vector3i getBlocEnMinage()           { return blocEnMinage; }
    public boolean isF3Actif()                  { return f3Actif; }
}
