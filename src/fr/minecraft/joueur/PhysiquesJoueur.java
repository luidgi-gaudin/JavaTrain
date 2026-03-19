package fr.minecraft.joueur;

import fr.minecraft.monde.Monde;
import fr.minecraft.physiques.AABB;
import org.joml.Vector3f;

public class PhysiquesJoueur {

    private static final float GRAVITE       = 20.0f;
    private static final float VITESSE_TERM  = -50.0f;
    private static final float VITESSE_SAUT  = 8.5f;

    public void mettreAJour(Joueur joueur, float dt, Monde monde) {
        if (joueur.isMort()) return;

        Vector3f vitesse = joueur.getVitesse();
        Vector3f pos     = joueur.getPosition();

        if (joueur.getMode() == Joueur.Mode.CREATIF && joueur.isVol()) {
            // Vol créatif : pas de gravité, pas de collision
            pos.x += vitesse.x * dt;
            pos.y += vitesse.y * dt;
            pos.z += vitesse.z * dt;
            joueur.setAuSol(false);
            return;
        }

        // Gravité
        vitesse.y = Math.max(VITESSE_TERM, vitesse.y - GRAVITE * dt);

        // Déplacement tentatif
        float dx = vitesse.x * dt;
        float dy = vitesse.y * dt;
        float dz = vitesse.z * dt;

        AABB aabb = joueur.getAABB();

        // Blocs candidates dans la zone étendue
        int x0 = (int) Math.floor(aabb.minX() + Math.min(dx, 0) - 1);
        int y0 = (int) Math.floor(aabb.minY() + Math.min(dy, 0) - 1);
        int z0 = (int) Math.floor(aabb.minZ() + Math.min(dz, 0) - 1);
        int x1 = (int) Math.ceil (aabb.maxX() + Math.max(dx, 0) + 1);
        int y1 = (int) Math.ceil (aabb.maxY() + Math.max(dy, 0) + 1);
        int z1 = (int) Math.ceil (aabb.maxZ() + Math.max(dz, 0) + 1);

        // Résoudre axe par axe
        for (int bx = x0; bx <= x1; bx++)
            for (int by = y0; by <= y1; by++)
                for (int bz = z0; bz <= z1; bz++)
                    if (monde.estSolide(bx, by, bz)) {
                        AABB bloc = new AABB(bx, by, bz, bx+1, by+1, bz+1);
                        dx = aabb.sweepX(bloc, dx);
                    }
        pos.x += dx;
        aabb = joueur.getAABB();

        for (int bx = x0; bx <= x1; bx++)
            for (int by = y0; by <= y1; by++)
                for (int bz = z0; bz <= z1; bz++)
                    if (monde.estSolide(bx, by, bz)) {
                        AABB bloc = new AABB(bx, by, bz, bx+1, by+1, bz+1);
                        dy = aabb.sweepY(bloc, dy);
                    }
        boolean auSol = (dy < vitesse.y * dt - 1e-4f && vitesse.y <= 0);
        if (auSol) vitesse.y = 0;
        pos.y += dy;
        aabb = joueur.getAABB();

        for (int bx = x0; bx <= x1; bx++)
            for (int by = y0; by <= y1; by++)
                for (int bz = z0; bz <= z1; bz++)
                    if (monde.estSolide(bx, by, bz)) {
                        AABB bloc = new AABB(bx, by, bz, bx+1, by+1, bz+1);
                        dz = aabb.sweepZ(bloc, dz);
                    }
        pos.z += dz;

        joueur.setAuSol(auSol);
        // Amortir vitesses horizontales
        if (auSol) { vitesse.x *= 0.75f; vitesse.z *= 0.75f; }
    }

    public void appliquerSaut(Joueur joueur) {
        if (joueur.isAuSol()) {
            joueur.getVitesse().y = VITESSE_SAUT;
            joueur.setAuSol(false);
        }
    }
}
