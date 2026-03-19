import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class Camera {
    private final Vector3f position;
    private float yaw;   // rotation horizontale (autour de Y), en degrés
    private float pitch; // rotation verticale (autour de X), en degrés

    private static final float VITESSE    = 8.0f;
    private static final float SENSIBILITE = 0.1f;
    private static final float FOV        = (float) Math.toRadians(70);

    private double dernierX, dernierY;
    private boolean premierMouvement = true;

    public Camera(float x, float y, float z) {
        position = new Vector3f(x, y, z);
        yaw   = -90.0f; // regarde vers -Z au départ
        pitch =   0.0f;
    }

    /** Appelé par le callback souris GLFW */
    public void gererSouris(double x, double y) {
        if (premierMouvement) {
            dernierX = x;
            dernierY = y;
            premierMouvement = false;
        }
        float dx = (float) (x - dernierX) * SENSIBILITE;
        float dy = (float) (dernierY - y) * SENSIBILITE; // inversé : souris haut = regard haut
        dernierX = x;
        dernierY = y;

        yaw   += dx;
        pitch += dy;
        pitch  = Math.max(-89.0f, Math.min(89.0f, pitch));
    }

    /** Déplacement WASD + Espace/Shift */
    public void gererClavier(long fenetre, float deltaTemps) {
        Vector3f avancer = direction();
        // Vecteur droite = avancer × haut
        Vector3f droite = avancer.cross(new Vector3f(0, 1, 0), new Vector3f()).normalize();
        float v = VITESSE * deltaTemps;

        if (GLFW.glfwGetKey(fenetre, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS)
            position.add(new Vector3f(avancer).mul(v));
        if (GLFW.glfwGetKey(fenetre, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS)
            position.sub(new Vector3f(avancer).mul(v));
        if (GLFW.glfwGetKey(fenetre, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS)
            position.sub(new Vector3f(droite).mul(v));
        if (GLFW.glfwGetKey(fenetre, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS)
            position.add(new Vector3f(droite).mul(v));
        if (GLFW.glfwGetKey(fenetre, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS)
            position.y += v;
        if (GLFW.glfwGetKey(fenetre, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS)
            position.y -= v;
    }

    /** Vecteur "avant" calculé depuis yaw et pitch */
    private Vector3f direction() {
        double yawRad   = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);
        return new Vector3f(
            (float) (Math.cos(pitchRad) * Math.cos(yawRad)),
            (float) (Math.sin(pitchRad)),
            (float) (Math.cos(pitchRad) * Math.sin(yawRad))
        ).normalize();
    }

    /** Matrice de vue (lookAt) */
    public Matrix4f getVue() {
        Vector3f dir   = direction();
        Vector3f cible = new Vector3f(position).add(dir);
        return new Matrix4f().lookAt(position, cible, new Vector3f(0, 1, 0));
    }

    /** Matrice de projection perspective */
    public Matrix4f getProjection(float largeur, float hauteur) {
        return new Matrix4f().perspective(FOV, largeur / hauteur, 0.1f, 500.0f);
    }
}
