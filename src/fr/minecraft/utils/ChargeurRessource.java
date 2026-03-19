package fr.minecraft.utils;

import java.io.InputStream;

public class ChargeurRessource {

    public static InputStream getFlux(String chemin) {
        InputStream is = ChargeurRessource.class.getResourceAsStream(chemin);
        if (is == null) is = ChargeurRessource.class.getResourceAsStream("/" + chemin);
        if (is == null) throw new RuntimeException("Ressource introuvable : " + chemin);
        return is;
    }

    public static String lireTexte(String chemin) {
        try (InputStream is = getFlux(chemin)) {
            return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Lecture ressource : " + chemin, e);
        }
    }
}
