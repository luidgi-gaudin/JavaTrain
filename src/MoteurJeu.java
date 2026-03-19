import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

public class MoteurJeu {
    private final long fenetre;
    private final float[] sommets = {
            // Premier triangle (Bas-Gauche)
            -0.5f,  0.5f, 0.0f,  // Haut-Gauche
            -0.5f, -0.5f, 0.0f,  // Bas-Gauche
            0.5f, -0.5f, 0.0f,  // Bas-Droit

            // Deuxième triangle (Haut-Droit)
            -0.5f,  0.5f, 0.0f,  // Haut-Gauche
            0.5f, -0.5f, 0.0f,  // Bas-Droit
            0.5f,  0.5f, 0.0f   // Haut-Droit
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
        GL20.glVertexAttribPointer(
                0,            // Index : l'emplacement (souvent 0 pour la position)
                3,            // Taille : 3 nombres par sommet (x, y, z)
                GL11.GL_FLOAT,// Type : ce sont des nombres décimaux
                false,        // Normalisé : non
                12,           // Stride : l'espace entre deux sommets (en octets)
                0             // Offset : le point de départ dans le buffer
        );
        GL20.glEnableVertexAttribArray(0);

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