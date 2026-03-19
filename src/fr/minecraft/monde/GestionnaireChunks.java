package fr.minecraft.monde;

import fr.minecraft.utils.CoordonneesChunk;
import org.joml.Vector3f;

import java.util.*;

public class GestionnaireChunks {

    private static final int DISTANCE_RENDU  = 8;  // en chunks
    private static final int DISTANCE_DECHAR = 10;
    private static final int MAX_BUILD_FRAME  = 2;

    private final Monde monde;
    private final GenerateurTerrain generateur;
    private final Queue<CoordonneesChunk> fileGeneration = new ArrayDeque<>();
    private final Set<CoordonneesChunk>   enAttente       = new HashSet<>();

    public GestionnaireChunks(Monde monde, long graine) {
        this.monde      = monde;
        this.generateur = new GenerateurTerrain(graine);
    }

    /** Appelé chaque frame avec la position du joueur. */
    public void mettreAJour(Vector3f posJoueur) {
        int joueurCX = Math.floorDiv((int) posJoueur.x, Chunk.LARGEUR);
        int joueurCZ = Math.floorDiv((int) posJoueur.z, Chunk.PROFONDEUR);

        // Planifier les chunks à charger (spirale)
        for (int r = 0; r <= DISTANCE_RENDU; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(dx) != r && Math.abs(dz) != r) continue;
                    CoordonneesChunk coords = new CoordonneesChunk(joueurCX + dx, joueurCZ + dz);
                    if (monde.getChunk(coords) == null && !enAttente.contains(coords)) {
                        enAttente.add(coords);
                        fileGeneration.offer(coords);
                    }
                }
            }
        }

        // Générer + construire max 2 chunks par frame
        int construit = 0;
        while (!fileGeneration.isEmpty() && construit < MAX_BUILD_FRAME) {
            CoordonneesChunk coords = fileGeneration.poll();
            enAttente.remove(coords);
            if (monde.getChunk(coords) != null) continue;

            Chunk chunk = new Chunk(coords.cx(), coords.cz());
            generateur.generer(chunk, coords.cx(), coords.cz());
            monde.ajouterChunk(coords, chunk);
            chunk.construireMaillage();
            construit++;
        }

        // Reconstruire les chunks dirty
        construit = 0;
        for (Chunk chunk : monde.getChunks().values()) {
            if (chunk.isDirty() && construit < MAX_BUILD_FRAME) {
                chunk.construireMaillage();
                construit++;
            }
        }

        // Décharger les chunks trop loin
        monde.getChunks().entrySet().removeIf(entry -> {
            CoordonneesChunk c = entry.getKey();
            int dist = Math.max(Math.abs(c.cx() - joueurCX), Math.abs(c.cz() - joueurCZ));
            if (dist > DISTANCE_DECHAR) {
                entry.getValue().supprimer();
                return true;
            }
            return false;
        });
    }
}
