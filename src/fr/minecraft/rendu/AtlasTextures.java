package fr.minecraft.rendu;

import fr.minecraft.blocs.TypeBloc;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Génère un atlas de textures procédural 256×256 RGBA (tuiles 16×16 px).
 * Chaque tuile correspond à un numéro de colonne dans l'atlas.
 */
public class AtlasTextures {

    private static final int ATLAS_SIZE  = 256;
    private static final int TILE_SIZE   = 16;
    private static final int TILES_PER_ROW = ATLAS_SIZE / TILE_SIZE;

    private final int textureId;

    public AtlasTextures() {
        ByteBuffer buf = MemoryUtil.memAlloc(ATLAS_SIZE * ATLAS_SIZE * 4);
        generer(buf);
        buf.flip();

        textureId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, ATLAS_SIZE, ATLAS_SIZE,
                          0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST_MIPMAP_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        MemoryUtil.memFree(buf);
    }

    private void generer(ByteBuffer buf) {
        // 16 types de tuiles en ligne 0
        TypeBloc[] blocsParColonne = {
            TypeBloc.AIR, TypeBloc.PIERRE, TypeBloc.HERBE, TypeBloc.TERRE,
            TypeBloc.SABLE, TypeBloc.BOIS, TypeBloc.HERBE, TypeBloc.FEUILLES,
            TypeBloc.MINERAI_FER, TypeBloc.OBSIDIENNE, TypeBloc.EAU, TypeBloc.BEDROCK,
            TypeBloc.AIR, TypeBloc.AIR, TypeBloc.AIR, TypeBloc.AIR
        };

        // Remplissage pixel par pixel
        for (int py = 0; py < ATLAS_SIZE; py++) {
            for (int px = 0; px < ATLAS_SIZE; px++) {
                int col   = px / TILE_SIZE;
                int ligne = py / TILE_SIZE;
                int lx    = px % TILE_SIZE;
                int ly    = py % TILE_SIZE;

                int tileIndex = ligne * TILES_PER_ROW + col;
                if (tileIndex >= blocsParColonne.length) {
                    // Tuile vide - noir transparent
                    buf.put((byte) 0); buf.put((byte) 0); buf.put((byte) 0); buf.put((byte) 0);
                    continue;
                }
                TypeBloc bloc = blocsParColonne[tileIndex];
                couleurTuile(buf, bloc, col, lx, ly);
            }
        }
    }

    private void couleurTuile(ByteBuffer buf, TypeBloc bloc, int col, int lx, int ly) {
        if (bloc == TypeBloc.AIR) {
            buf.put((byte) 0); buf.put((byte) 0); buf.put((byte) 0); buf.put((byte) 0);
            return;
        }

        // Bruit de surface simple déterministe
        int hash = (lx * 13 + ly * 7 + col * 31) & 0xFF;
        int bruit = (hash % 20) - 10; // -10 à +9

        // Couleur de base
        float r = bloc.r, g = bloc.g, b = bloc.b;

        // Variation pour herbe col 0 (face haut) et col 2 (face côté)
        if (bloc == TypeBloc.HERBE && col == 0) {
            // face haut : vert herbe avec détails
            r = clamp(0.35f + bruit * 0.004f);
            g = clamp(0.70f + bruit * 0.006f);
            b = clamp(0.15f + bruit * 0.002f);
        } else if (bloc == TypeBloc.HERBE && col == 3) {
            // côtés herbe-terre
            r = clamp(TypeBloc.TERRE.r + bruit * 0.003f);
            g = clamp(TypeBloc.TERRE.g + bruit * 0.003f);
            b = clamp(TypeBloc.TERRE.b + bruit * 0.002f);
            // ligne verte en haut pour les côtés
            if (ly < 3) {
                r = clamp(0.35f + bruit * 0.004f);
                g = clamp(0.68f + bruit * 0.005f);
                b = clamp(0.15f);
            }
        } else if (bloc == TypeBloc.PIERRE) {
            r = clamp(r + bruit * 0.003f);
            g = clamp(g + bruit * 0.003f);
            b = clamp(b + bruit * 0.003f);
        } else if (bloc == TypeBloc.SABLE) {
            r = clamp(r + bruit * 0.005f);
            g = clamp(g + bruit * 0.004f);
            b = clamp(b + bruit * 0.002f);
        } else if (bloc == TypeBloc.EAU) {
            r = clamp(r + bruit * 0.002f);
            g = clamp(g + bruit * 0.003f);
            b = clamp(b + bruit * 0.003f);
        } else {
            r = clamp(r + bruit * 0.003f);
            g = clamp(g + bruit * 0.003f);
            b = clamp(b + bruit * 0.003f);
        }

        byte alpha = (bloc == TypeBloc.EAU || bloc == TypeBloc.FEUILLES) ? (byte) 180 : (byte) 255;
        buf.put((byte)(r * 255)); buf.put((byte)(g * 255)); buf.put((byte)(b * 255)); buf.put(alpha);
    }

    private float clamp(float v) { return Math.max(0, Math.min(1, v)); }

    public void lier(int unite) {
        org.lwjgl.opengl.GL13.glActiveTexture(org.lwjgl.opengl.GL13.GL_TEXTURE0 + unite);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
    }

    public void supprimer() {
        GL11.glDeleteTextures(textureId);
    }
}
