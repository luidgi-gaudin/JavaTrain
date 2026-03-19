package fr.minecraft.utils;

public record CoordonneesChunk(int cx, int cz) {

    public static CoordonneesChunk depuis(int mondeX, int mondeZ) {
        return new CoordonneesChunk(Math.floorDiv(mondeX, 16), Math.floorDiv(mondeZ, 16));
    }
}
