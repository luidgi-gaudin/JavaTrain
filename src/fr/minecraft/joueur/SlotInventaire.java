package fr.minecraft.joueur;

import fr.minecraft.blocs.TypeBloc;

public record SlotInventaire(TypeBloc type, int quantite) {

    public static final SlotInventaire VIDE = new SlotInventaire(TypeBloc.AIR, 0);

    public boolean estVide() { return type == TypeBloc.AIR || quantite <= 0; }

    public SlotInventaire ajouter(int n) {
        return new SlotInventaire(type, quantite + n);
    }

    public SlotInventaire retirer(int n) {
        int q = quantite - n;
        return q <= 0 ? VIDE : new SlotInventaire(type, q);
    }
}
