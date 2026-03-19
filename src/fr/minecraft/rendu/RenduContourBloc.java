package fr.minecraft.rendu;

import org.joml.Matrix4f;
import org.joml.Vector3i;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

/**
 * Wireframe légèrement expansé (0.005) autour du bloc sélectionné.
 */
public class RenduContourBloc {

    private static final String VERT =
        "#version 330 core\n" +
        "layout(location=0) in vec3 pos;\n" +
        "uniform mat4 mvp;\n" +
        "void main() { gl_Position = mvp * vec4(pos, 1.0); }\n";

    private static final String FRAG =
        "#version 330 core\n" +
        "out vec4 c;\n" +
        "void main() { c = vec4(0.0, 0.0, 0.0, 0.7); }\n";

    private final Shader shader;
    private final int vaoId, vboId;

    public RenduContourBloc() {
        shader = new Shader(VERT, FRAG);
        vaoId  = GL30.glGenVertexArrays();
        vboId  = GL15.glGenBuffers();
    }

    public void rendu(Vector3i pos, Matrix4f mvp) {
        float e = 0.005f;
        float x0 = pos.x - e, y0 = pos.y - e, z0 = pos.z - e;
        float x1 = pos.x + 1 + e, y1 = pos.y + 1 + e, z1 = pos.z + 1 + e;

        float[] lignes = {
            x0,y0,z0, x1,y0,z0,  x1,y0,z0, x1,y0,z1,  x1,y0,z1, x0,y0,z1,  x0,y0,z1, x0,y0,z0,
            x0,y1,z0, x1,y1,z0,  x1,y1,z0, x1,y1,z1,  x1,y1,z1, x0,y1,z1,  x0,y1,z1, x0,y1,z0,
            x0,y0,z0, x0,y1,z0,  x1,y0,z0, x1,y1,z0,  x1,y0,z1, x1,y1,z1,  x0,y0,z1, x0,y1,z1
        };

        FloatBuffer buf = MemoryUtil.memAllocFloat(lignes.length);
        for (float f : lignes) buf.put(f);
        buf.flip();

        GL30.glBindVertexArray(vaoId);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buf, GL15.GL_DYNAMIC_DRAW);
        MemoryUtil.memFree(buf);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 12, 0);
        GL20.glEnableVertexAttribArray(0);

        shader.utiliser();
        shader.setUniformMat4("mvp", mvp);

        GL11.glLineWidth(1.5f);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDrawArrays(GL11.GL_LINES, 0, lignes.length / 3);
        GL11.glDisable(GL11.GL_BLEND);
        GL30.glBindVertexArray(0);
    }

    public void supprimer() {
        shader.supprimer();
        GL15.glDeleteBuffers(vboId);
        GL30.glDeleteVertexArrays(vaoId);
    }
}
