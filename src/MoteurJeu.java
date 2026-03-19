import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

public class MoteurJeu {

    // -------------------------------------------------------------------------
    // Shaders GLSL (OpenGL 3.3 Core)
    // -------------------------------------------------------------------------
    private static final String VERTEX_SHADER =
        "#version 330 core\n" +
        "layout(location = 0) in vec3 position;\n" +
        "layout(location = 1) in vec3 couleur;\n" +
        "layout(location = 2) in vec3 normale;\n" +
        "\n" +
        "out vec3 vCouleur;\n" +
        "out vec3 vNormale;\n" +
        "\n" +
        "uniform mat4 mvp;\n" +
        "\n" +
        "void main() {\n" +
        "    gl_Position = mvp * vec4(position, 1.0);\n" +
        "    vCouleur = couleur;\n" +
        "    vNormale = normale;\n" +
        "}\n";

    private static final String FRAGMENT_SHADER =
        "#version 330 core\n" +
        "in vec3 vCouleur;\n" +
        "in vec3 vNormale;\n" +
        "\n" +
        "out vec4 couleurSortie;\n" +
        "\n" +
        "void main() {\n" +
        "    // Lumière directionnelle (soleil légèrement de côté)\n" +
        "    vec3 dirSoleil = normalize(vec3(0.5, -1.0, 0.3));\n" +
        "    float diffuse  = max(dot(vNormale, -dirSoleil), 0.0);\n" +
        "    float lumiere  = 0.30 + 0.70 * diffuse;\n" +
        "    couleurSortie  = vec4(vCouleur * lumiere, 1.0);\n" +
        "}\n";

    // -------------------------------------------------------------------------
    private final long fenetre;
    private Camera camera;
    private Shader shader;
    private Chunk chunk;

    private int largeur = 1280;
    private int hauteur  = 720;
    private long dernierTemps;

    public MoteurJeu(long fenetre) {
        this.fenetre = fenetre;
    }

    public void run() {
        GLFW.glfwMakeContextCurrent(fenetre);
        GLFW.glfwSwapInterval(1); // VSync activé
        GL.createCapabilities();

        // Configuration OpenGL
        GL11.glClearColor(0.52f, 0.80f, 0.98f, 1.0f); // bleu ciel
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);

        // Capture du curseur (mode FPS)
        GLFW.glfwSetInputMode(fenetre, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);

        // Caméra : position au-dessus du centre du chunk
        camera = new Camera(8, 12, 8);

        shader = new Shader(VERTEX_SHADER, FRAGMENT_SHADER);

        // Génération du terrain et construction du mesh
        chunk = new Chunk();
        genererTerrain(chunk);
        chunk.construireMaillage();

        // Callbacks
        GLFW.glfwSetCursorPosCallback(fenetre, (win, x, y) -> camera.gererSouris(x, y));
        GLFW.glfwSetFramebufferSizeCallback(fenetre, (win, w, h) -> {
            largeur = w;
            hauteur  = h;
            GL11.glViewport(0, 0, w, h);
        });

        dernierTemps = System.nanoTime();

        // Boucle principale
        while (!GLFW.glfwWindowShouldClose(fenetre)) {
            long maintenant = System.nanoTime();
            float deltaTemps = (maintenant - dernierTemps) / 1_000_000_000.0f;
            dernierTemps = maintenant;

            miseAJour(deltaTemps);
            rendu();
        }

        chunk.supprimer();
        shader.supprimer();
    }

    // -------------------------------------------------------------------------
    // Génération du terrain
    // -------------------------------------------------------------------------
    private void genererTerrain(Chunk chunk) {
        for (int x = 0; x < Chunk.LARGEUR; x++) {
            for (int z = 0; z < Chunk.PROFONDEUR; z++) {
                // Hauteur variée par une combinaison de sinus/cosinus
                double nx = x / (double) Chunk.LARGEUR;
                double nz = z / (double) Chunk.PROFONDEUR;
                int hauteurSol = 5 + (int) (
                    Math.sin(nx * Math.PI * 2.0) * 2.5 +
                    Math.cos(nz * Math.PI * 3.0) * 2.0 +
                    Math.sin((nx + nz) * Math.PI * 2.5) * 1.5
                );
                hauteurSol = Math.max(2, hauteurSol);

                // Remplissage colonne : pierre en bas, terre au milieu, herbe en surface
                for (int y = 0; y < hauteurSol - 2; y++)
                    chunk.setBloc(x, y, z, TypeBloc.PIERRE);
                chunk.setBloc(x, hauteurSol - 2, z, TypeBloc.PIERRE);
                chunk.setBloc(x, hauteurSol - 1, z, TypeBloc.TERRE);
                chunk.setBloc(x, hauteurSol,     z, TypeBloc.HERBE);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Mise à jour
    // -------------------------------------------------------------------------
    private void miseAJour(float deltaTemps) {
        GLFW.glfwPollEvents();
        camera.gererClavier(fenetre, deltaTemps);

        if (GLFW.glfwGetKey(fenetre, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS)
            GLFW.glfwSetWindowShouldClose(fenetre, true);
    }

    // -------------------------------------------------------------------------
    // Rendu
    // -------------------------------------------------------------------------
    private void rendu() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        shader.utiliser();

        // Calcul de la matrice MVP (Model-View-Projection)
        Matrix4f projection = camera.getProjection(largeur, hauteur);
        Matrix4f vue        = camera.getVue();
        Matrix4f mvp        = new Matrix4f();
        projection.mul(vue, mvp); // mvp = projection * vue

        // Envoi de la matrice au shader
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buf = stack.mallocFloat(16);
            mvp.get(buf);
            GL20.glUniformMatrix4fv(shader.getUniformLocation("mvp"), false, buf);
        }

        chunk.rendu();

        GLFW.glfwSwapBuffers(fenetre);
    }
}
