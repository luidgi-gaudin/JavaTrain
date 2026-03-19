package fr.minecraft.rendu;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;

public class Shader {

    private final int programmeId;

    public Shader(String sourceVertex, String sourceFragment) {
        int vid = compiler(GL20.GL_VERTEX_SHADER,   sourceVertex);
        int fid = compiler(GL20.GL_FRAGMENT_SHADER, sourceFragment);
        programmeId = GL20.glCreateProgram();
        GL20.glAttachShader(programmeId, vid);
        GL20.glAttachShader(programmeId, fid);
        GL20.glLinkProgram(programmeId);
        if (GL20.glGetProgrami(programmeId, GL20.GL_LINK_STATUS) == 0)
            throw new RuntimeException("Erreur liaison shader:\n" + GL20.glGetProgramInfoLog(programmeId));
        GL20.glDeleteShader(vid);
        GL20.glDeleteShader(fid);
    }

    public static Shader fromClasspath(String vertPath, String fragPath) {
        return new Shader(lire(vertPath), lire(fragPath));
    }

    private static String lire(String chemin) {
        try {
            InputStream is = Shader.class.getResourceAsStream(chemin);
            if (is == null) is = Shader.class.getResourceAsStream("/" + chemin);
            if (is == null) throw new RuntimeException("Shader introuvable : " + chemin);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Lecture shader : " + chemin, e);
        }
    }

    private int compiler(int type, String source) {
        int id = GL20.glCreateShader(type);
        GL20.glShaderSource(id, source);
        GL20.glCompileShader(id);
        if (GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS) == 0)
            throw new RuntimeException("Erreur compilation shader:\n" + GL20.glGetShaderInfoLog(id));
        return id;
    }

    public void utiliser() { GL20.glUseProgram(programmeId); }
    public void arreter()  { GL20.glUseProgram(0); }

    public int getUniformLocation(String nom) {
        return GL20.glGetUniformLocation(programmeId, nom);
    }

    public void setUniformMat4(String nom, Matrix4f mat) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buf = stack.mallocFloat(16);
            mat.get(buf);
            GL20.glUniformMatrix4fv(getUniformLocation(nom), false, buf);
        }
    }

    public void setUniformVec3(String nom, float x, float y, float z) {
        GL20.glUniform3f(getUniformLocation(nom), x, y, z);
    }

    public void setUniformFloat(String nom, float val) {
        GL20.glUniform1f(getUniformLocation(nom), val);
    }

    public void setUniformInt(String nom, int val) {
        GL20.glUniform1i(getUniformLocation(nom), val);
    }

    public void supprimer() { GL20.glDeleteProgram(programmeId); }
}
