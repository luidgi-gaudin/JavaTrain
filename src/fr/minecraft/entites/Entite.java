package fr.minecraft.entites;

import fr.minecraft.monde.Monde;
import fr.minecraft.physiques.AABB;
import org.joml.Vector3f;

public abstract class Entite {

    protected Vector3f position;
    protected Vector3f vitesse;
    protected float largeur, hauteur;
    protected boolean auSol;
    protected float yaw;
    protected boolean mort = false;

    public Entite(float x, float y, float z, float largeur, float hauteur) {
        this.position = new Vector3f(x, y, z);
        this.vitesse  = new Vector3f(0, 0, 0);
        this.largeur  = largeur;
        this.hauteur  = hauteur;
    }

    public abstract void mettreAJour(float dt, Monde monde);

    protected void appliquerPhysiques(float dt, Monde monde) {
        // Gravité
        vitesse.y = Math.max(-30f, vitesse.y - 18f * dt);

        float dx = vitesse.x * dt;
        float dy = vitesse.y * dt;
        float dz = vitesse.z * dt;

        AABB aabb = getAABB();
        float hw = largeur / 2f;
        int x0 = (int) Math.floor(position.x - hw + Math.min(dx,0) - 1);
        int y0 = (int) Math.floor(position.y + Math.min(dy,0) - 1);
        int z0 = (int) Math.floor(position.z - hw + Math.min(dz,0) - 1);
        int x1 = (int) Math.ceil (position.x + hw + Math.max(dx,0) + 1);
        int y1 = (int) Math.ceil (position.y + hauteur + Math.max(dy,0) + 1);
        int z1 = (int) Math.ceil (position.z + hw + Math.max(dz,0) + 1);

        for (int bx=x0;bx<=x1;bx++) for (int by=y0;by<=y1;by++) for (int bz=z0;bz<=z1;bz++)
            if (monde.estSolide(bx,by,bz)) dx = aabb.sweepX(new AABB(bx,by,bz,bx+1,by+1,bz+1), dx);
        position.x += dx; aabb = getAABB();

        for (int bx=x0;bx<=x1;bx++) for (int by=y0;by<=y1;by++) for (int bz=z0;bz<=z1;bz++)
            if (monde.estSolide(bx,by,bz)) dy = aabb.sweepY(new AABB(bx,by,bz,bx+1,by+1,bz+1), dy);
        auSol = dy < vitesse.y * dt - 1e-4f && vitesse.y <= 0;
        if (auSol) { vitesse.y = 0; vitesse.x *= 0.7f; vitesse.z *= 0.7f; }
        position.y += dy; aabb = getAABB();

        for (int bx=x0;bx<=x1;bx++) for (int by=y0;by<=y1;by++) for (int bz=z0;bz<=z1;bz++)
            if (monde.estSolide(bx,by,bz)) dz = aabb.sweepZ(new AABB(bx,by,bz,bx+1,by+1,bz+1), dz);
        position.z += dz;
    }

    public AABB getAABB() {
        float hw = largeur / 2f;
        return new AABB(position.x-hw, position.y, position.z-hw,
                        position.x+hw, position.y+hauteur, position.z+hw);
    }

    public Vector3f getPosition() { return position; }
    public float getYaw()         { return yaw; }
    public boolean estMort()      { return mort; }
}
