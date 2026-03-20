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
    // fenetre : identifiant (handle) de la fenêtre créée par GLFW
    private final long fenetre;

    private float angle = 0.0f;

    private Vector3f cameraPos   = new Vector3f(0.0f, 0.0f, 3.0f); // position
    private Vector3f cameraFront = new Vector3f(0.0f, 0.0f, -1.0f); // Là où je regardes
    private Vector3f cameraUp    = new Vector3f(0.0f, 1.0f, 0.0f); // Le "haut" du monde
    private float vitesseCamera  = 0.05f;

    private float yaw = -90.0f; // Rotation gauche/droite
    private float pitch = 0.0f;  // Rotation haut/bas
    private double lastX = 400, lastY = 300; // Position précédente de la souris

    // sommets : données brutes envoyées à la carte graphique
    // On dessine ici 2 triangles pour former un carré.
    // Chaque ligne représente un sommet avec : Position (X, Y, Z) et Couleur (R, G, B)
    // On ne définit que les 8 coins uniques du cube
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

    // La "recette" pour assembler les triangles en utilisant les numéros ci-dessus
    private final int[] indices = {
            0, 1, 2, 2, 3, 0, // Face Avant
            4, 5, 6, 6, 7, 4, // Face Arrière
            4, 5, 1, 1, 0, 4, // Face Gauche
            3, 2, 6, 6, 7, 3, // Face Droite
            0, 3, 7, 7, 4, 0, // Face Haut
            1, 5, 6, 6, 2, 1  // Face Bas
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

        GL11.glEnable(GL11.GL_DEPTH_TEST);

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

        //creation d'un buffer pour l'ebo
        int eboId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, eboId);
        //on envoie les indices cela vas permettre d'optimiser nos blocks
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices, GL15.GL_STATIC_DRAW);


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


        int uniModel = GL20.glGetUniformLocation(shaderProgram, "model");

        int uniView = GL20.glGetUniformLocation(shaderProgram, "view");

        //bloque le curseur dans la fenetre et le rend invisible
        GLFW.glfwSetInputMode(fenetre, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);

        // Boucle de jeu (Loop)
        while (!GLFW.glfwWindowShouldClose(fenetre)) {

            update(fenetre);


            // --- MISE À JOUR ---
            angle += 0.05f; // Vitesse de rotation (ajuste à ta guise)

            // On crée la matrice "Model" qui place l'objet
            // On recule de -2.0f et on tourne sur l'axe Y (0, 1, 0)
            Matrix4f model = new Matrix4f()
                    .translate(0, 0, -2.0f)
                    .rotate((float) Math.toRadians(angle), 0, 1, 0);


            // On regarde de : cameraPos
            // Vers : cameraPos + cameraFront (ce qui est devant nous)
            Vector3f cible = new Vector3f(cameraPos).add(cameraFront);
            Matrix4f view = new Matrix4f().lookAt(cameraPos, cible, cameraUp);

            // --- ENVOI AU SHADER ---
            // On doit activer le programme avant d'envoyer un Uniform
            GL20.glUseProgram(shaderProgram);

            try (MemoryStack stack = MemoryStack.stackPush()) {
                FloatBuffer fb = stack.mallocFloat(16);

                //envoie de la matrice model
                model.get(fb);
                GL20.glUniformMatrix4fv(uniModel, false, fb);

                //envoie de la matrice de vue
                view.get(fb);
                GL20.glUniformMatrix4fv(uniView, false, fb);

                DoubleBuffer xpos = stack.mallocDouble(1);
                DoubleBuffer ypos = stack.mallocDouble(1);
                GLFW.glfwGetCursorPos(fenetre, xpos, ypos);

                double x = xpos.get(0);
                double y = ypos.get(0);

                float offsetX = (float) (x - lastX);
                float offsetY = (float) (lastY - y); // Inversé car le Y va du haut vers le bas
                lastX = x;
                lastY = y;

                float sensibilite = 0.1f;
                yaw   += offsetX * sensibilite;
                pitch += offsetY * sensibilite;

                // On bloque le regard pour ne pas se briser la nuque à 90°
                if (pitch > 89.0f) pitch = 89.0f;
                if (pitch < -89.0f) pitch = -89.0f;

                // Calcul du nouveau vecteur de direction
                Vector3f direction = new Vector3f();
                direction.x = (float) (Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
                direction.y = (float) Math.sin(Math.toRadians(pitch));
                direction.z = (float) (Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
                cameraFront = direction.normalize();
            }

            // --- RENDU ---
            render();
        }
    }

    private void update(long window) {
        GLFW.glfwPollEvents();

        // Avancer (Z)
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) {
            cameraPos.add(new Vector3f(cameraFront).mul(vitesseCamera));
        }
        // Reculer (S)
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) {
            cameraPos.sub(new Vector3f(cameraFront).mul(vitesseCamera));
        }
        // Gauche (Q)
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS) {
            Vector3f gauche = new Vector3f(cameraFront).cross(cameraUp).normalize();
            cameraPos.sub(gauche.mul(vitesseCamera));
        }
        // Droite (D)
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS) {
            Vector3f droite = new Vector3f(cameraFront).cross(cameraUp).normalize();
            cameraPos.add(droite.mul(vitesseCamera));
        }
    }

    private void render() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        // On dessine en utilisant les éléments (indices)
        // 36 : le nombre total d'indices
        // GL_UNSIGNED_INT : car nos indices sont des 'int'
        // 0 : on commence au début du buffer d'indices
        GL20.glDrawElements(GL11.GL_TRIANGLES, 36, GL11.GL_UNSIGNED_INT, 0);

        GLFW.glfwSwapBuffers(fenetre);
    }
}