package fr.minecraft.joueur;

import fr.minecraft.blocs.TypeBloc;

public class Inventaire {

    public static final int SLOTS_HOTBAR = 9;
    public static final int SLOTS_SAC    = 27;

    private final SlotInventaire[] hotbar = new SlotInventaire[SLOTS_HOTBAR];
    private final SlotInventaire[] sac    = new SlotInventaire[SLOTS_SAC];
    private int slotActif = 0;

    public Inventaire() {
        java.util.Arrays.fill(hotbar, SlotInventaire.VIDE);
        java.util.Arrays.fill(sac,    SlotInventaire.VIDE);
        // Pré-remplir hotbar en mode créatif
        TypeBloc[] predefinis = { TypeBloc.HERBE, TypeBloc.TERRE, TypeBloc.PIERRE,
            TypeBloc.SABLE, TypeBloc.BOIS, TypeBloc.FEUILLES, TypeBloc.MINERAI_FER, TypeBloc.OBSIDIENNE, TypeBloc.BEDROCK };
        for (int i = 0; i < predefinis.length; i++)
            hotbar[i] = new SlotInventaire(predefinis[i], 64);
    }

    public SlotInventaire getHotbar(int i)         { return hotbar[i]; }
    public SlotInventaire getSlotActif()            { return hotbar[slotActif]; }
    public TypeBloc getBlocActif()                  { return hotbar[slotActif].type(); }
    public int getIndexSlotActif()                  { return slotActif; }
    public void setSlotActif(int i)                 { slotActif = Math.max(0, Math.min(SLOTS_HOTBAR-1, i)); }

    public void ajouterBloc(TypeBloc type, int n) {
        // Chercher slot existant dans hotbar
        for (int i = 0; i < SLOTS_HOTBAR; i++) {
            if (hotbar[i].type() == type && hotbar[i].quantite() < 64) {
                hotbar[i] = hotbar[i].ajouter(n);
                return;
            }
        }
        // Slot vide dans hotbar
        for (int i = 0; i < SLOTS_HOTBAR; i++) {
            if (hotbar[i].estVide()) { hotbar[i] = new SlotInventaire(type, n); return; }
        }
        // Sac
        for (int i = 0; i < SLOTS_SAC; i++) {
            if (sac[i].type() == type && sac[i].quantite() < 64) { sac[i] = sac[i].ajouter(n); return; }
        }
        for (int i = 0; i < SLOTS_SAC; i++) {
            if (sac[i].estVide()) { sac[i] = new SlotInventaire(type, n); return; }
        }
    }

    public boolean consommerBlocActif() {
        if (hotbar[slotActif].estVide()) return false;
        hotbar[slotActif] = hotbar[slotActif].retirer(1);
        return true;
    }

    public void remplirInfini() {
        for (int i = 0; i < SLOTS_HOTBAR; i++)
            if (!hotbar[i].estVide())
                hotbar[i] = new SlotInventaire(hotbar[i].type(), 64);
    }
}
