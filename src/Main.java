import org.lwjgl.glfw.GLFW;

public class Main {
    public static void main(String[] args) {
        // Initialisation de GLFW
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Impossible d'initialiser GLFW");
        }

        // Création de la fenêtre
        long fenetre = GLFW.glfwCreateWindow(800, 600, "Mon Minecraft", 0, 0);
        if (fenetre == 0) {
            throw new RuntimeException("Échec de la création de la fenêtre");
        }

        // Lancement du moteur sur le thread principal
        MoteurJeu jeu = new MoteurJeu(fenetre);
        jeu.run();

        // Nettoyage à la fermeture
        GLFW.glfwTerminate();
    }
}