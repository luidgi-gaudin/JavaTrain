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
    /** Identifiant de la fenêtre GLFW. */
    private final long fenetre;
    /** Angle de rotation pour l'animation. */
    private float angle = 0.0f;
    //endregion

    //region Caméra et Souris
    /** Position de la caméra dans le monde. */
    private Vector3f cameraPos   = new Vector3f(0.0f, 0.0f, 3.0f);
    /** Direction dans laquelle la caméra regarde. */
    private Vector3f cameraFront = new Vector3f(0.0f, 0.0f, -1.0f);
    /** Vecteur orienté vers le "haut" du monde. */
    private Vector3f cameraUp    = new Vector3f(0.0f, 1.0f, 0.0f);
    /** Vitesse de déplacement de la caméra. */
    private float vitesseCamera  = 0.05f;
    /** Flag pour la première entrée de la souris. */
    private boolean firstMouse = true;
    /** Rotation horizontale (gauche/droite). */
    private float yaw = -90.0f;
    /** Rotation verticale (haut/bas). */
    private float pitch = 0.0f;
    /** Dernière position X de la souris. */
    private double lastX = 400;
    /** Dernière position Y de la souris. */
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
     * Gère l'initialisation OpenGL, la boucle d'événements et le rendu.
     */
    public void run() {
        // --- INITIALISATION OPENGL ---
        GLFW.glfwMakeContextCurrent(fenetre);
        GL.createCapabilities();
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        // 1. Initialisation des Shaders
        String vertex = chargerShaders("ressources/shaders/bloc.vert");
        String fragment = chargerShaders("ressources/shaders/bloc.frag");

        int vertexShader = compileShader(vertex, GL20.GL_VERTEX_SHADER);
        int fragmentShader = compileShader(fragment, GL20.GL_FRAGMENT_SHADER);

        // Création et liaison du programme shader
        int shaderProgram = GL20.glCreateProgram();
        GL20.glAttachShader(shaderProgram, vertexShader);
        GL20.glAttachShader(shaderProgram, fragmentShader);
        GL20.glLinkProgram(shaderProgram);

        if (GL20.glGetProgrami(shaderProgram, GL20.GL_LINK_STATUS) == 0) {
            throw new RuntimeException("Erreur de lien : " + GL20.glGetProgramInfoLog(shaderProgram));
        }

        // Nettoyage des shaders compilés individuellement
        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);

        // Configuration du rendu initial
        GL11.glClearColor(0.5f, 0.8f, 1.0f, 1.0f);

        // 2. Gestion des Buffers (VBO & EBO)
        int vboId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, sommets, GL15.GL_STATIC_DRAW);

        int eboId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboId);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices, GL15.GL_STATIC_DRAW);

        // Définition de la structure des données (Layout)
        // Attribut 0 : Position (3 float, décalage 0)
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 24, 0);
        GL20.glEnableVertexAttribArray(0);

        // Attribut 1 : Couleur (3 float, décalage 12 octets)
        GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 24, 12);
        GL20.glEnableVertexAttribArray(1);

        // 3. Configuration des Uniforms et Matrices
        GL20.glUseProgram(shaderProgram);
        int uniProjection = GL20.glGetUniformLocation(shaderProgram, "projection");
        int uniModel = GL20.glGetUniformLocation(shaderProgram, "model");
        int uniView = GL20.glGetUniformLocation(shaderProgram, "view");

        // Matrice de projection perspective
        Matrix4f projection = new Matrix4f().perspective((float) Math.toRadians(70.0f), 800.0f/600.0f, 0.1f, 100.0f);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16);
            projection.get(fb);
            GL20.glUniformMatrix4fv(uniProjection, false, fb);
        }

        // Configuration de la souris
        GLFW.glfwSetInputMode(fenetre, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);

        // --- BOUCLE DE JEU ---
        while (!GLFW.glfwWindowShouldClose(fenetre)) {
            // Mise à jour de l'état du jeu et entrées utilisateur
            update(fenetre);

            // Calcul de la rotation automatique du cube
            angle += 0.05f;
            Matrix4f model = new Matrix4f()
                    .translate(0, 0, -2.0f)
                    .rotate((float) Math.toRadians(angle), 0, 1, 0);

            // Mise à jour de la vue (Caméra)
            Vector3f cible = new Vector3f(cameraPos).add(cameraFront);
            Matrix4f view = new Matrix4f().lookAt(cameraPos, cible, cameraUp);

            // Envoi des matrices à la carte graphique
            GL20.glUseProgram(shaderProgram);
            try (MemoryStack stack = MemoryStack.stackPush()) {
                FloatBuffer fb = stack.mallocFloat(16);

                model.get(fb);
                GL20.glUniformMatrix4fv(uniModel, false, fb);

                view.get(fb);
                GL20.glUniformMatrix4fv(uniView, false, fb);

                // Gestion du mouvement de la souris pour la caméra
                DoubleBuffer xpos = stack.mallocDouble(1);
                DoubleBuffer ypos = stack.mallocDouble(1);
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
                yaw   += offsetX * sensibilite;
                pitch += offsetY * sensibilite;

                // Limitation de la rotation verticale (nuque)
                if (pitch > 89.0f) pitch = 89.0f;
                if (pitch < -89.0f) pitch = -89.0f;

                // Calcul du vecteur de direction de la caméra
                Vector3f direction = new Vector3f();
                direction.x = (float) (Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
                direction.y = (float) Math.sin(Math.toRadians(pitch));
                direction.z = (float) (Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
                cameraFront = direction.normalize();
            }

            // Rendu de la scène
            render();
        }
    }

    /**
     * Gère les entrées clavier pour le déplacement de la caméra.
     * @param window Handle de la fenêtre.
     */
    private void update(long window) {
        GLFW.glfwPollEvents();

        // Déplacement : Z+
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) {
            cameraPos.add(new Vector3f(cameraFront).mul(vitesseCamera));
        }
        // Déplacement : Z-
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) {
            cameraPos.sub(new Vector3f(cameraFront).mul(vitesseCamera));
        }
        // Déplacement : Gauche (Strafe)
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS) {
            Vector3f gauche = new Vector3f(cameraFront).cross(cameraUp).normalize();
            cameraPos.sub(gauche.mul(vitesseCamera));
        }
        // Déplacement : Droite (Strafe)
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS) {
            Vector3f droite = new Vector3f(cameraFront).cross(cameraUp).normalize();
            cameraPos.add(droite.mul(vitesseCamera));
        }
    }

    /**
     * Effectue les opérations de rendu OpenGL.
     */
    private void render() {
        // Effacement de l'écran (Couleur et Profondeur)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        // Dessin du cube via les indices (EBO)
        // 36 indices pour 12 triangles (6 faces)
        GL20.glDrawElements(GL11.GL_TRIANGLES, 36, GL11.GL_UNSIGNED_INT, 0);

        // Affichage du buffer de rendu
        GLFW.glfwSwapBuffers(fenetre);
    }
    //endregion
}