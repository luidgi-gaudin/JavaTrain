import org.lwjgl.glfw.GLFW;

public class Main {
    //region --- POINT D'ENTRÉE ---
    /**
     * Point d'entrée principal de l'application.
     * Initialise GLFW, crée la fenêtre et lance le moteur de jeu.
     * @param args Arguments de la ligne de commande.
     */
    public static void main(String[] args) {
        // 1. Initialisation de GLFW (Graphics Library Framework)
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Impossible d'initialiser GLFW");
        }

        // 2. Création de la fenêtre (800x600 pixels)
        long fenetre = GLFW.glfwCreateWindow(800, 600, "Mon Minecraft", 0, 0);
        if (fenetre == 0) {
            throw new RuntimeException("Échec de la création de la fenêtre");
        }

        // 3. Lancement du moteur de jeu
        MoteurJeu jeu = new MoteurJeu(fenetre);
        jeu.run();

        // 4. Nettoyage final
        GLFW.glfwTerminate();
    }
    //endregion
}