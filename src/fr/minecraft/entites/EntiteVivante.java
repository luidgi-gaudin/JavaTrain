package fr.minecraft.entites;

import fr.minecraft.monde.Monde;

public abstract class EntiteVivante extends Entite {

    protected float sante;
    protected final float santeMax;
    protected float timerInvincibilite = 0;

    public EntiteVivante(float x, float y, float z, float largeur, float hauteur, float santeMax) {
        super(x, y, z, largeur, hauteur);
        this.santeMax = santeMax;
        this.sante    = santeMax;
    }

    public void infliger(float degats) {
        if (timerInvincibilite > 0) return;
        sante -= degats;
        timerInvincibilite = 0.5f;
        if (sante <= 0) mourir();
    }

    protected void mourir() {
        mort = true;
        surMort();
    }

    protected abstract void surMort();

    @Override
    public void mettreAJour(float dt, Monde monde) {
        timerInvincibilite = Math.max(0, timerInvincibilite - dt);
    }

    public float getSante() { return sante; }
}
