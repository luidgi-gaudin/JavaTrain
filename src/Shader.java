import org.lwjgl.opengl.GL20;

public class Shader {
    private final int programmeId;

    public Shader(String sourceVertex, String sourceFragment) {
        int vertexId = compiler(GL20.GL_VERTEX_SHADER, sourceVertex);
        int fragmentId = compiler(GL20.GL_FRAGMENT_SHADER, sourceFragment);

        programmeId = GL20.glCreateProgram();
        GL20.glAttachShader(programmeId, vertexId);
        GL20.glAttachShader(programmeId, fragmentId);
        GL20.glLinkProgram(programmeId);

        if (GL20.glGetProgrami(programmeId, GL20.GL_LINK_STATUS) == 0) {
            throw new RuntimeException("Erreur de liaison du shader:\n" + GL20.glGetProgramInfoLog(programmeId));
        }

        GL20.glDeleteShader(vertexId);
        GL20.glDeleteShader(fragmentId);
    }

    private int compiler(int type, String source) {
        int id = GL20.glCreateShader(type);
        GL20.glShaderSource(id, source);
        GL20.glCompileShader(id);

        if (GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS) == 0) {
            throw new RuntimeException("Erreur de compilation du shader:\n" + GL20.glGetShaderInfoLog(id));
        }
        return id;
    }

    public void utiliser() {
        GL20.glUseProgram(programmeId);
    }

    public void arreter() {
        GL20.glUseProgram(0);
    }

    public int getUniformLocation(String nom) {
        return GL20.glGetUniformLocation(programmeId, nom);
    }

    public void supprimer() {
        GL20.glDeleteProgram(programmeId);
    }
}
