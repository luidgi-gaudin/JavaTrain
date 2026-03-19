import org.lwjgl.glfw.GLFW;

public class Main {
    public static void main(String[] args) {
        // Initialisation de GLFW (Graphics Library Framework)
        // C'est une bibliothèque qui permet de créer des fenêtres, gérer les entrées (clavier, souris)
        // et créer un contexte OpenGL de manière portable.
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Impossible d'initialiser GLFW");
        }

        // Création de la fenêtre
        // 800x600 pixels, Titre de la fenêtre, 0 pour le moniteur (fenêtré), 0 pour le partage de ressources
        long fenetre = GLFW.glfwCreateWindow(800, 600, "Mon Minecraft", 0, 0);
        if (fenetre == 0) {
            throw new RuntimeException("Échec de la création de la fenêtre");
        }

        // Lancement du moteur sur le thread principal
        // On passe l'identifiant de la fenêtre au moteur pour qu'il puisse dessiner dedans.
        MoteurJeu jeu = new MoteurJeu(fenetre);
        jeu.run();

        // Nettoyage à la fermeture pour libérer la mémoire utilisée par GLFW
        GLFW.glfwTerminate();
    }
}