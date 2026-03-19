import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;

public class MoteurJeu {
    // fenetre : identifiant (handle) de la fenêtre créée par GLFW
    private final long fenetre;

    private float angle = 0.0f;

    // sommets : données brutes envoyées à la carte graphique
    // On dessine ici 2 triangles pour former un carré.
    // Chaque ligne représente un sommet avec : Position (X, Y, Z) et Couleur (R, G, B)
    private final float[] sommets = {
            // Position (X, Y, Z)          // Couleur (R, G, B)
            -0.5f,  0.5f, -2.0f,           1.0f, 0.0f, 0.0f,  // Haut-Gauche
            -0.5f, -0.5f, -2.0f,           0.0f, 1.0f, 0.0f,  // Bas-Gauche
            0.5f, -0.5f, -2.0f,           0.0f, 0.0f, 1.0f,  // Bas-Droit

            -0.5f,  0.5f, -2.0f,           1.0f, 0.0f, 0.0f,  // Haut-Gauche
            0.5f, -0.5f, -2.0f,           0.0f, 0.0f, 1.0f,  // Bas-Droit
            0.5f,  0.5f, -2.0f,           1.0f, 1.0f, 0.0f   // Haut-Droit
    };

    /**
     * Lit un fichier texte (shader) et le retourne sous forme de String.
     */
    private String chargerShaders(String path){
        StringBuilder contenuShaders = new StringBuilder();

        try {
            FileReader lecteur = new FileReader(path);
            BufferedReader lecteurLigne = new BufferedReader(lecteur);
            String ligne;
            while ((ligne = lecteurLigne.readLine()) != null) {
                contenuShaders.append(ligne + " \n ");
            }
            lecteurLigne.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return contenuShaders.toString();
    }

    /**
     * Compile le code source d'un shader (Vertex ou Fragment).
     */
    private int compileShader(String source, int type) {
        // Crée un objet shader vide du type spécifié
        int shaderId = GL20.glCreateShader(type);
        // Envoie le code source au shader
        GL20.glShaderSource(shaderId, source);
        // Demande à la carte graphique de compiler le code
        GL20.glCompileShader(shaderId);

        // Vérification d'erreurs éventuelles de compilation
        if (GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == 0) {
            throw new RuntimeException("Erreur de compilation shader : " + GL20.glGetShaderInfoLog(shaderId));
        }

        return shaderId;
    }

    public MoteurJeu(long fenetre) {
        this.fenetre = fenetre;
    }

    public void run() {
        // Liaison du contexte OpenGL à ce thread (indispensable avant d'utiliser des fonctions GL)
        GLFW.glfwMakeContextCurrent(fenetre);
        // Initialisation de la bibliothèque LWJGL pour OpenGL
        GL.createCapabilities();

        // 1. Chargement des fichiers sources des shaders
        String vertex = chargerShaders("ressources/shaders/bloc.vert");
        String fragment = chargerShaders("ressources/shaders/bloc.frag");

        // 2. Compilation des shaders individuels
        int vertexShader = compileShader(vertex, GL20.GL_VERTEX_SHADER);
        int fragmentShader = compileShader(fragment, GL20.GL_FRAGMENT_SHADER);

        // 3. Création du Programme Shader qui lie le Vertex et le Fragment Shader ensemble
        int shaderProgram = GL20.glCreateProgram();
        GL20.glAttachShader(shaderProgram, vertexShader);
        GL20.glAttachShader(shaderProgram, fragmentShader);
        GL20.glLinkProgram(shaderProgram);

        // Vérifier si la liaison (link) a réussi
        if (GL20.glGetProgrami(shaderProgram, GL20.GL_LINK_STATUS) == 0) {
            throw new RuntimeException("Erreur de lien : " + GL20.glGetProgramInfoLog(shaderProgram));
        }

        // Nettoyage des shaders individuels (ils sont maintenant dans le shaderProgram)
        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);

        // Couleur de fond de la fenêtre (Ciel bleu ici)
        GL11.glClearColor(0.5f, 0.8f, 1.0f, 1.0f);

        // 4. Création d'un Vertex Buffer Object (VBO) pour stocker les sommets en mémoire vidéo
        int vboId = GL15.glGenBuffers();

        // On "active" le buffer
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        // On envoie les données du tableau 'sommets' vers le buffer actif sur la carte graphique
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, sommets, GL15.GL_STATIC_DRAW);

        // 5. Configuration des attributs (Comment lire le tableau de sommets)
        // stride = 24 octets (6 floats * 4 octets) pour passer au sommet suivant

        // Attribut 0 : Position (3 floats)
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 24, 0);
        GL20.glEnableVertexAttribArray(0);

        // Attribut 1 : Couleur (3 floats), commence après les 3 floats de position (12 octets)
        GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 24, 12);
        GL20.glEnableVertexAttribArray(1);

        // On active le programme shader pour le rendu
        GL20.glUseProgram(shaderProgram);

        // On récupère l'emplacement de la variable "projection" définie dans bloc.vert
        int uniProjection = GL20.glGetUniformLocation(shaderProgram, "projection");

        // On crée la matrice (70° FOV, Ratio 800/600, Proche: 0.1, Loin: 100.0)
        Matrix4f projection = new Matrix4f().perspective((float) Math.toRadians(70.0f), 800.0f/600.0f, 0.1f, 100.0f);

        // On envoie la matrice à la carte graphique
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16);
            projection.get(fb);
            GL20.glUniformMatrix4fv(uniProjection, false, fb);
        }

        // Juste après uniProjection
        int uniModel = GL20.glGetUniformLocation(shaderProgram, "model");

        // Boucle de jeu (Loop)
        while (!GLFW.glfwWindowShouldClose(fenetre)) {
            // --- MISE À JOUR ---
            angle += 0.05f; // Vitesse de rotation (ajuste à ta guise)

            // On crée la matrice "Model" qui place l'objet
            // On recule de -2.0f et on tourne sur l'axe Y (0, 1, 0)
            Matrix4f model = new Matrix4f()
                    .translate(0, 0, -2.0f)
                    .rotate((float) Math.toRadians(angle), 0, 1, 0);

            // --- ENVOI AU SHADER ---
            // On doit activer le programme avant d'envoyer un Uniform
            GL20.glUseProgram(shaderProgram);

            try (MemoryStack stack = MemoryStack.stackPush()) {
                FloatBuffer fb = stack.mallocFloat(16);
                model.get(fb);
                GL20.glUniformMatrix4fv(uniModel, false, fb);
            }

            // --- RENDU ---
            update();
            render();
        }
    }

    private void update() {
        // On récupère les entrées clavier/souris pour éviter que la fenêtre ne freeze
        GLFW.glfwPollEvents();
    }

    private void render() {
        // Efface l'écran (couleur de fond + profondeur)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        // Dessine les triangles à partir des données dans le buffer actif
        // 6 sommets au total (2 triangles)
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
        // Affiche l'image calculée à l'écran (Double buffering)
        GLFW.glfwSwapBuffers(fenetre);
    }
}