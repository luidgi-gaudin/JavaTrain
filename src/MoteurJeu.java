import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

public class MoteurJeu {
    private final long fenetre;
    private final float[] sommets = {
            // Position (X, Y, Z)    // Couleur (R, G, B)
            -0.5f,  0.5f, 0.0f,      1.0f, 0.0f, 0.0f,  // Haut-Gauche (Rouge)
            -0.5f, -0.5f, 0.0f,      0.0f, 1.0f, 0.0f,  // Bas-Gauche  (Vert)
            0.5f, -0.5f, 0.0f,      0.0f, 0.0f, 1.0f,  // Bas-Droit   (Bleu)

            -0.5f,  0.5f, 0.0f,      1.0f, 0.0f, 0.0f,  // Haut-Gauche (Rouge)
            0.5f, -0.5f, 0.0f,      0.0f, 0.0f, 1.0f,  // Bas-Droit   (Bleu)
            0.5f,  0.5f, 0.0f,      1.0f, 1.0f, 0.0f   // Haut-Droit  (Jaune)
    };

    public MoteurJeu(long fenetre) {
        this.fenetre = fenetre;
    }

    public void run() {

        // Liaison du contexte OpenGL à ce thread
        GLFW.glfwMakeContextCurrent(fenetre);
        GL.createCapabilities();

        GL11.glClearColor(0.5f, 0.8f, 1.0f, 1.0f);

        int vboId = GL15.glGenBuffers();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, sommets, GL15.GL_STATIC_DRAW);

        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 24, 0);
        GL20.glEnableVertexAttribArray(0);

        GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 24, 12);
        GL20.glEnableVertexAttribArray(1);

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
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
        GLFW.glfwSwapBuffers(fenetre);
    }
}