package fr.minecraft.rendu;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

/**
 * Quad plein-écran NDC dessiné en premier (depthMask=false) avec un gradient ciel.
 */
public class RenduCiel {

    private static final String VERT =
        "#version 330 core\n" +
        "layout(location=0) in vec2 pos;\n" +
        "out vec2 vPos;\n" +
        "void main() { vPos = pos; gl_Position = vec4(pos, 0.9999, 1.0); }\n";

    private static final String FRAG =
        "#version 330 core\n" +
        "in vec2 vPos;\n" +
        "out vec4 couleur;\n" +
        "uniform vec3 couleurZenith;\n" +
        "uniform vec3 couleurHorizon;\n" +
        "void main() {\n" +
        "    float t = clamp(vPos.y * 0.5 + 0.5, 0.0, 1.0);\n" +
        "    couleur = vec4(mix(couleurHorizon, couleurZenith, t*t), 1.0);\n" +
        "}\n";

    private final Shader shader;
    private final int vaoId, vboId;

    public RenduCiel() {
        shader = new Shader(VERT, FRAG);

        float[] quad = { -1,-1,  1,-1,  1,1,  -1,-1,  1,1,  -1,1 };
        FloatBuffer buf = MemoryUtil.memAllocFloat(quad.length);
        for (float f : quad) buf.put(f);
        buf.flip();

        vaoId = GL30.glGenVertexArrays();
        vboId = GL15.glGenBuffers();
        GL30.glBindVertexArray(vaoId);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buf, GL15.GL_STATIC_DRAW);
        MemoryUtil.memFree(buf);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 8, 0);
        GL20.glEnableVertexAttribArray(0);
        GL30.glBindVertexArray(0);
    }

    public void rendu() {
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_CULL_FACE);
        shader.utiliser();
        shader.setUniformVec3("couleurZenith",  0.20f, 0.45f, 0.85f);
        shader.setUniformVec3("couleurHorizon", 0.65f, 0.82f, 0.98f);
        GL30.glBindVertexArray(vaoId);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
        GL30.glBindVertexArray(0);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDepthMask(true);
    }

    public void supprimer() {
        shader.supprimer();
        GL15.glDeleteBuffers(vboId);
        GL30.glDeleteVertexArrays(vaoId);
    }
}
