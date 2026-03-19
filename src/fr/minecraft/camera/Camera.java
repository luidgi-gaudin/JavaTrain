package fr.minecraft.camera;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class Camera {

    private final Vector3f position;
    private float yaw;
    private float pitch;

    private static final float VITESSE     = 8.0f;
    private static final float SENSIBILITE = 0.1f;
    private static final float FOV         = (float) Math.toRadians(70);

    private double dernierX, dernierY;
    private boolean premierMouvement = true;

    public Camera(float x, float y, float z) {
        position = new Vector3f(x, y, z);
        yaw   = -90.0f;
        pitch =   0.0f;
    }

    public void gererSouris(double x, double y) {
        if (premierMouvement) { dernierX = x; dernierY = y; premierMouvement = false; return; }
        float dx = (float)(x - dernierX) * SENSIBILITE;
        float dy = (float)(dernierY - y) * SENSIBILITE;
        dernierX = x;
        dernierY = y;
        yaw   += dx;
        pitch  = Math.max(-89.0f, Math.min(89.0f, pitch + dy));
    }

    public void gererClavier(long fenetre, float dt) {
        Vector3f avancer = getDirection();
        Vector3f droite  = new Vector3f(avancer).cross(new Vector3f(0,1,0)).normalize();
        float v = VITESSE * dt;
        if (GLFW.glfwGetKey(fenetre, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) position.add(new Vector3f(avancer).mul(v));
        if (GLFW.glfwGetKey(fenetre, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) position.sub(new Vector3f(avancer).mul(v));
        if (GLFW.glfwGetKey(fenetre, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS) position.sub(new Vector3f(droite).mul(v));
        if (GLFW.glfwGetKey(fenetre, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS) position.add(new Vector3f(droite).mul(v));
        if (GLFW.glfwGetKey(fenetre, GLFW.GLFW_KEY_SPACE)      == GLFW.GLFW_PRESS) position.y += v;
        if (GLFW.glfwGetKey(fenetre, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS) position.y -= v;
    }

    public Vector3f getDirection() {
        double yR = Math.toRadians(yaw), pR = Math.toRadians(pitch);
        return new Vector3f(
            (float)(Math.cos(pR) * Math.cos(yR)),
            (float) Math.sin(pR),
            (float)(Math.cos(pR) * Math.sin(yR))
        ).normalize();
    }

    public Vector3f getPosition()      { return new Vector3f(position); }
    public void setPosition(float x, float y, float z) { position.set(x, y, z); }
    public float getYaw()              { return yaw; }
    public float getPitch()            { return pitch; }

    public Matrix4f getVue() {
        Vector3f dir = getDirection();
        return new Matrix4f().lookAt(position, new Vector3f(position).add(dir), new Vector3f(0,1,0));
    }

    public Matrix4f getProjection(float largeur, float hauteur) {
        return new Matrix4f().perspective(FOV, largeur/hauteur, 0.1f, 500.0f);
    }
}
