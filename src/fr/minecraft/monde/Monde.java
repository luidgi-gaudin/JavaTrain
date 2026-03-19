package fr.minecraft.monde;

import fr.minecraft.blocs.TypeBloc;
import fr.minecraft.entites.Entite;
import fr.minecraft.utils.CoordonneesChunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Monde {

    private final Map<CoordonneesChunk, Chunk> chunks = new HashMap<>();
    private final List<Entite> entites = new ArrayList<>();

    public void ajouterChunk(CoordonneesChunk coords, Chunk chunk) {
        chunk.setMonde(this);
        chunks.put(coords, chunk);
    }

    public Chunk getChunk(int cx, int cz) {
        return chunks.get(new CoordonneesChunk(cx, cz));
    }

    public Chunk getChunk(CoordonneesChunk coords) {
        return chunks.get(coords);
    }

    public Map<CoordonneesChunk, Chunk> getChunks() { return chunks; }

    /** Accès cross-chunk : coordonnées monde. */
    public TypeBloc getBloc(int mx, int my, int mz) {
        if (my < 0 || my >= Chunk.HAUTEUR) return TypeBloc.AIR;
        int cx = Math.floorDiv(mx, Chunk.LARGEUR);
        int cz = Math.floorDiv(mz, Chunk.PROFONDEUR);
        Chunk c = chunks.get(new CoordonneesChunk(cx, cz));
        if (c == null) return TypeBloc.AIR;
        int lx = Math.floorMod(mx, Chunk.LARGEUR);
        int lz = Math.floorMod(mz, Chunk.PROFONDEUR);
        return c.getBloc(lx, my, lz);
    }

    public void setBloc(int mx, int my, int mz, TypeBloc type) {
        int cx = Math.floorDiv(mx, Chunk.LARGEUR);
        int cz = Math.floorDiv(mz, Chunk.PROFONDEUR);
        Chunk c = chunks.get(new CoordonneesChunk(cx, cz));
        if (c == null) return;
        int lx = Math.floorMod(mx, Chunk.LARGEUR);
        int lz = Math.floorMod(mz, Chunk.PROFONDEUR);
        c.setBloc(lx, my, lz, type);
        // Marquer chunks voisins dirty si le bloc est sur une bordure
        marquerVoisinsSiBordure(cx, cz, lx, lz);
    }

    private void marquerVoisinsSiBordure(int cx, int cz, int lx, int lz) {
        if (lx == 0)                    markDirty(cx-1, cz);
        if (lx == Chunk.LARGEUR-1)      markDirty(cx+1, cz);
        if (lz == 0)                    markDirty(cx, cz-1);
        if (lz == Chunk.PROFONDEUR-1)   markDirty(cx, cz+1);
    }

    private void markDirty(int cx, int cz) {
        Chunk c = chunks.get(new CoordonneesChunk(cx, cz));
        if (c != null) c.setBloc(0, 0, 0, c.getBloc(0, 0, 0)); // force dirty flag
    }

    // ---- Entités ----
    public void ajouterEntite(Entite e)   { entites.add(e); }
    public List<Entite> getEntites()      { return entites; }

    public void mettreAJourEntites(float dt) {
        entites.removeIf(e -> { e.mettreAJour(dt, this); return e.estMort(); });
    }

    public boolean estSolide(int mx, int my, int mz) {
        return getBloc(mx, my, mz).estSolide();
    }

    public void supprimer() {
        chunks.values().forEach(Chunk::supprimer);
        chunks.clear();
    }
}
