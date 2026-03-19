package fr.minecraft.rendu;

import fr.minecraft.entites.Entite;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.List;

/**
 * Rendu des entités : cube 1×1 couleur unie + shader avec matrice modèle.
 */
public class RenduEntite {

    private static final String VERT =
        "#version 330 core\n" +
        "layout(location=0) in vec3 pos;\n" +
        "uniform mat4 mvp;\n" +
        "uniform vec3 couleur;\n" +
        "out vec3 vCoul;\n" +
        "void main() { vCoul = couleur; gl_Position = mvp * vec4(pos, 1.0); }\n";

    private static final String FRAG =
        "#version 330 core\n" +
        "in vec3 vCoul; out vec4 c;\n" +
        "void main() { c = vec4(vCoul, 1.0); }\n";

    private final Shader shader;
    private final int vaoId, vboId;
    private static final int NB_SOMMETS = 36;

    public RenduEntite() {
        shader = new Shader(VERT, FRAG);
        float[] cube = cubeVertices(0,0,0,1,1,1);
        FloatBuffer buf = MemoryUtil.memAllocFloat(cube.length);
        for (float f : cube) buf.put(f);
        buf.flip();
        vaoId = GL30.glGenVertexArrays();
        vboId = GL15.glGenBuffers();
        GL30.glBindVertexArray(vaoId);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buf, GL15.GL_STATIC_DRAW);
        MemoryUtil.memFree(buf);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 12, 0);
        GL20.glEnableVertexAttribArray(0);
        GL30.glBindVertexArray(0);
    }

    public void rendreEntites(List<Entite> entites, Matrix4f vp, float[] couleursParType) {
        shader.utiliser();
        GL30.glBindVertexArray(vaoId);
        for (Entite e : entites) {
            Matrix4f model = new Matrix4f()
                .translate(e.getPosition())
                .rotateY((float) Math.toRadians(e.getYaw()));
            Matrix4f mvp = new Matrix4f(vp).mul(model);
            shader.setUniformMat4("mvp", mvp);
            // Couleur selon type
            float r = 0.3f, g = 0.3f, b = 0.3f;
            if (e instanceof fr.minecraft.entites.mobs.Zombie)  { r=0.2f; g=0.5f; b=0.2f; }
            if (e instanceof fr.minecraft.entites.mobs.Vache)   { r=0.8f; g=0.7f; b=0.5f; }
            if (e instanceof fr.minecraft.entites.EntiteItem)   { r=0.9f; g=0.9f; b=0.1f; }
            shader.setUniformVec3("couleur", r, g, b);
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, NB_SOMMETS);
        }
        GL30.glBindVertexArray(0);
    }

    private float[] cubeVertices(float x0,float y0,float z0,float x1,float y1,float z1) {
        return new float[]{
            x0,y0,z0,x1,y0,z0,x1,y1,z0, x0,y0,z0,x1,y1,z0,x0,y1,z0,
            x0,y0,z1,x0,y1,z1,x1,y1,z1, x0,y0,z1,x1,y1,z1,x1,y0,z1,
            x0,y1,z0,x0,y1,z1,x1,y1,z1, x0,y1,z0,x1,y1,z1,x1,y1,z0,
            x0,y0,z0,x1,y0,z1,x0,y0,z1, x0,y0,z0,x1,y0,z0,x1,y0,z1,
            x1,y0,z0,x1,y0,z1,x1,y1,z1, x1,y0,z0,x1,y1,z1,x1,y1,z0,
            x0,y0,z0,x0,y1,z0,x0,y1,z1, x0,y0,z0,x0,y1,z1,x0,y0,z1,
        };
    }

    public void supprimer() {
        shader.supprimer();
        GL15.glDeleteBuffers(vboId);
        GL30.glDeleteVertexArrays(vaoId);
    }
}
