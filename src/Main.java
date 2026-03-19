import org.lwjgl.glfw.GLFW;

public class Main {
    public static void main(String[] args) {
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Impossible d'initialiser GLFW");
        }

        // OpenGL 3.3 Core Profile
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);

        long fenetre = GLFW.glfwCreateWindow(1280, 720, "Mon Minecraft", 0, 0);
        if (fenetre == 0) {
            throw new RuntimeException("Échec de la création de la fenêtre");
        }

        MoteurJeu jeu = new MoteurJeu(fenetre);
        jeu.run();

        GLFW.glfwTerminate();
    }
}