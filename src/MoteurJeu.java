import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

public class MoteurJeu {
    //region --- ATTRIBUTS ---
    //region Fenêtre et Contrôle
    private final long fenetre;
    private int shaderProgram;
    private int uniProjection, uniModel, uniView;
    private float angle = 0.0f;
    //endregion

    //region Caméra et Souris
    private Vector3f cameraPos   = new Vector3f(0.0f, 0.0f, 3.0f);
    private Vector3f cameraFront = new Vector3f(0.0f, 0.0f, -1.0f);
    private Vector3f cameraUp    = new Vector3f(0.0f, 1.0f, 0.0f);
    private float vitesseCamera  = 0.05f;
    private boolean firstMouse = true;
    private float yaw = -90.0f;
    private float pitch = 0.0f;
    private double lastX = 400;
    private double lastY = 300;
    //endregion

    //region Géométrie du Cube
    /** Données brutes des sommets : Position (X, Y, Z) et Couleur (R, G, B). */
    private final float[] sommets = {
            // Position (X, Y, Z)    // Couleur (R, G, B)
            -0.5f,  0.5f,  0.5f,     1.0f, 0.0f, 0.0f, // 0: Avant-Haut-Gauche
            -0.5f, -0.5f,  0.5f,     0.0f, 1.0f, 0.0f, // 1: Avant-Bas-Gauche
            0.5f, -0.5f,  0.5f,     0.0f, 0.0f, 1.0f, // 2: Avant-Bas-Droit
            0.5f,  0.5f,  0.5f,     1.0f, 1.0f, 0.0f, // 3: Avant-Haut-Droit
            -0.5f,  0.5f, -0.5f,     1.0f, 0.0f, 1.0f, // 4: Arrière-Haut-Gauche
            -0.5f, -0.5f, -0.5f,     0.0f, 1.0f, 1.0f, // 5: Arrière-Bas-Gauche
            0.5f, -0.5f, -0.5f,     1.0f, 1.0f, 1.0f, // 6: Arrière-Bas-Droit
            0.5f,  0.5f, -0.5f,     0.0f, 0.0f, 0.0f  // 7: Arrière-Haut-Droit
    };

    /** Indices des sommets pour assembler les triangles (EBO). */
    private final int[] indices = {
            0, 1, 2, 2, 3, 0, // Face Avant
            4, 5, 6, 6, 7, 4, // Face Arrière
            4, 5, 1, 1, 0, 4, // Face Gauche
            3, 2, 6, 6, 7, 3, // Face Droite
            0, 3, 7, 7, 4, 0, // Face Haut
            1, 5, 6, 6, 2, 1  // Face Bas
    };
    //endregion

    //endregion

    //region --- CONSTRUCTEUR ---
    /**
     * Initialise le moteur de jeu avec la fenêtre spécifiée.
     * @param fenetre Identifiant de la fenêtre GLFW.
     */
    public MoteurJeu(long fenetre) {
        this.fenetre = fenetre;
    }
    //endregion

    //region --- GESTION DES SHADERS ---
    /**
     * Lit un fichier texte (shader) et le retourne sous forme de String.
     * @param path Chemin relatif du fichier de shader.
     * @return Le contenu du shader sous forme de chaîne de caractères.
     */
    private String chargerShaders(String path){
        StringBuilder contenuShaders = new StringBuilder();

        try {
            FileReader lecteur = new FileReader(path);
            BufferedReader lecteurLigne = new BufferedReader(lecteur);
            String ligne;
            while ((ligne = lecteurLigne.readLine()) != null) {
                contenuShaders.append(ligne).append(" \n ");
            }
            lecteurLigne.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return contenuShaders.toString();
    }

    /**
     * Compile le code source d'un shader (Vertex ou Fragment).
     * @param source Code source du shader.
     * @param type Type du shader (GL_VERTEX_SHADER ou GL_FRAGMENT_SHADER).
     * @return L'identifiant du shader compilé.
     */
    private int compileShader(String source, int type) {
        // Crée un objet shader vide
        int shaderId = GL20.glCreateShader(type);
        // Envoie le code source
        GL20.glShaderSource(shaderId, source);
        // Compilation par le driver graphique
        GL20.glCompileShader(shaderId);

        // Vérification des erreurs de compilation
        if (GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == 0) {
            throw new RuntimeException("Erreur de compilation shader : " + GL20.glGetShaderInfoLog(shaderId));
        }

        return shaderId;
    }
    //endregion

    //region --- CŒUR DU MOTEUR ---
    /**
     * Démarre la boucle principale du jeu.
     * Gère l'initialisation et le cycle de rendu.
     */
    public void run() {
        init();

        while (!GLFW.glfwWindowShouldClose(fenetre)) {
            loop();
        }

        cleanup();
    }

    /**
     * Initialisation globale des composants OpenGL.
     */
    private void init() {
        GLFW.glfwMakeContextCurrent(fenetre);
        GL.createCapabilities();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glClearColor(0.5f, 0.8f, 1.0f, 1.0f);

        initShaders();
        initBuffers();
        initMatrices();

        GLFW.glfwSetInputMode(fenetre, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
    }

    /**
     * Initialise et compile les shaders.
     */
    private void initShaders() {
        String vertex = chargerShaders("ressources/shaders/bloc.vert");
        String fragment = chargerShaders("ressources/shaders/bloc.frag");

        int vertexShader = compileShader(vertex, GL20.GL_VERTEX_SHADER);
        int fragmentShader = compileShader(fragment, GL20.GL_FRAGMENT_SHADER);

        shaderProgram = GL20.glCreateProgram();
        GL20.glAttachShader(shaderProgram, vertexShader);
        GL20.glAttachShader(shaderProgram, fragmentShader);
        GL20.glLinkProgram(shaderProgram);

        if (GL20.glGetProgrami(shaderProgram, GL20.GL_LINK_STATUS) == 0) {
            throw new RuntimeException("Erreur de lien : " + GL20.glGetProgramInfoLog(shaderProgram));
        }

        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);
    }

    /**
     * Initialise les buffers de données (VBO et EBO).
     */
    private void initBuffers() {
        int vboId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, sommets, GL15.GL_STATIC_DRAW);

        int eboId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboId);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices, GL15.GL_STATIC_DRAW);

        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 24, 0);
        GL20.glEnableVertexAttribArray(0);

        GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 24, 12);
        GL20.glEnableVertexAttribArray(1);
    }

    /**
     * Configure les matrices et récupère les emplacements des uniforms.
     */
    private void initMatrices() {
        GL20.glUseProgram(shaderProgram);
        uniProjection = GL20.glGetUniformLocation(shaderProgram, "projection");
        uniModel = GL20.glGetUniformLocation(shaderProgram, "model");
        uniView = GL20.glGetUniformLocation(shaderProgram, "view");

        Matrix4f projection = new Matrix4f().perspective((float) Math.toRadians(70.0f), 800.0f/600.0f, 0.1f, 100.0f);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16);
            projection.get(fb);
            GL20.glUniformMatrix4fv(uniProjection, false, fb);
        }
    }

    /**
     * Boucle de rendu et de mise à jour.
     */
    private void loop() {
        update(fenetre);

        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL20.glUseProgram(shaderProgram);

        Vector3f cible = new Vector3f(cameraPos).add(cameraFront);
        Matrix4f view = new Matrix4f().lookAt(cameraPos, cible, cameraUp);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16);

            DoubleBuffer xpos = stack.mallocDouble(1);
            DoubleBuffer ypos = stack.mallocDouble(1);
            updateSouris(fenetre, xpos, ypos);

            view.get(fb);
            GL20.glUniformMatrix4fv(uniView, false, fb);

            for (int x = -10; x < 10; x++) {
                for (int z = -10; z < 10; z++) {
                    dessinerBloc(x, -1, z, fb);
                }
            }
        }

        GLFW.glfwSwapBuffers(fenetre);
    }

    /**
     * Gère les entrées clavier pour le déplacement de la caméra.
     */
    private void update(long fenetre) {
        GLFW.glfwPollEvents();

        if (GLFW.glfwGetKey(fenetre, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) {
            cameraPos.add(new Vector3f(cameraFront).mul(vitesseCamera));
        }
        if (GLFW.glfwGetKey(fenetre, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) {
            cameraPos.sub(new Vector3f(cameraFront).mul(vitesseCamera));
        }
        if (GLFW.glfwGetKey(fenetre, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS) {
            Vector3f gauche = new Vector3f(cameraFront).cross(cameraUp).normalize();
            cameraPos.sub(gauche.mul(vitesseCamera));
        }
        if (GLFW.glfwGetKey(fenetre, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS) {
            Vector3f droite = new Vector3f(cameraFront).cross(cameraUp).normalize();
            cameraPos.add(droite.mul(vitesseCamera));
        }
    }

    /**
     * Met à jour l'orientation de la caméra via la souris.
     */
    private void updateSouris(long fenetre, DoubleBuffer xpos, DoubleBuffer ypos) {
        GLFW.glfwGetCursorPos(fenetre, xpos, ypos);
        double x = xpos.get(0);
        double y = ypos.get(0);

        if (firstMouse) {
            lastX = x;
            lastY = y;
            firstMouse = false;
        }

        float offsetX = (float) (x - lastX);
        float offsetY = (float) (lastY - y);
        lastX = x;
        lastY = y;

        float sensibilite = 0.1f;
        yaw += offsetX * sensibilite;
        pitch += offsetY * sensibilite;

        if (pitch > 89.0f) pitch = 89.0f;
        if (pitch < -89.0f) pitch = -89.0f;

        Vector3f direction = new Vector3f();
        direction.x = (float) (Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        direction.y = (float) Math.sin(Math.toRadians(pitch));
        direction.z = (float) (Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        cameraFront = direction.normalize();
    }

    /**
     * Dessine un bloc unique à des coordonnées spécifiques.
     */
    private void dessinerBloc(int x, int y, int z, FloatBuffer fb) {
        Matrix4f model = new Matrix4f().translate(x, y, z);
        model.get(fb);
        GL20.glUniformMatrix4fv(uniModel, false, fb);
        GL11.glDrawElements(GL11.GL_TRIANGLES, 36, GL11.GL_UNSIGNED_INT, 0);
    }

    /**
     * Libère les ressources avant la fermeture.
     */
    private void cleanup() {
        GL20.glDeleteProgram(shaderProgram);
    }
    //endregion
}