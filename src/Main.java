import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args){
        Canvas canvas = new Canvas();
        JFrame fenetre = new JFrame("Minecraft fait par mes soins");
        fenetre.setSize(800, 600);
        fenetre.add(canvas);
        fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fenetre.setVisible(true);

        MoteurJeu jeu = new MoteurJeu(canvas);
        jeu.demarrer();
    }
}
