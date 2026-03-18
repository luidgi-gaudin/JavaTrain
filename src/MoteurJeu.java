public class MoteurJeu implements Runnable {
    private boolean enMarche = false;

    public void demarrer() {
        enMarche = true;
        new Thread(this).start(); // Lance la méthode run() en arrière-plan
    }

    @Override
    public void run() {
        int limiteFps = 60;
        long tempsCibleNano = 1000000000 / limiteFps;
        while (enMarche) {
            long tempsDebut = System.nanoTime();

            update(); // 1. Mettre à jour la logique (physique, position)
            render(); // 2. Afficher (OpenGL interviendra ici plus tard)

            long tempsRestant = tempsCibleNano - (System.nanoTime() - tempsDebut);
            if (tempsRestant > 0){
                try {
                    Thread.sleep(tempsRestant / 1000000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void update() { /* Logique du jeu */ }
    private void render() { /* Affichage graphique */ }
}