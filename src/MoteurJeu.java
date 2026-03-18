import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

public class MoteurJeu {
    private final long fenetre;

    public MoteurJeu(long fenetre) {
        this.fenetre = fenetre;
    }

    public void run() {
        // Liaison du contexte OpenGL à ce thread
        GLFW.glfwMakeContextCurrent(fenetre);
        GL.createCapabilities();

        GL11.glClearColor(0.5f, 0.8f, 1.0f, 1.0f);

        // Boucle de jeu
        while (!GLFW.glfwWindowShouldClose(fenetre)) {
            update();
            render();
        }
    }

    private void update() {
        // On récupère les entrées clavier/souris
        GLFW.glfwPollEvents();
    }

    private void render() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        // --- Futur code de dessin ici ---

        GLFW.glfwSwapBuffers(fenetre);
    }
}