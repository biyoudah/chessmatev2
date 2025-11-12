package fr.univlorraine.pierreludmannchessmate;

public class DefiDames extends Defi {
    private int nombreDames;
    private static final long TIMEOUT_MS = 5000; // Timeout 5 secondes

    public DefiDames(String nom, String description, int difficulte, Joueur createur, int nombreDames) {
        super(nom, description, difficulte, createur);
        this.nombreDames = nombreDames;
    }

    @Override
    public boolean validerSolution(Echiquier echiquier) {
        long startTime = System.currentTimeMillis();
        int taille = echiquier.getTaille();
        int[] positions = new int[nombreDames];
        for (int i = 0; i < nombreDames; i++) {
            positions[i] = -1;
        }
        return backtrack(echiquier, positions, 0, taille, startTime);
    }

    @Override
    public int calculerScore(int tempsResolution) {
        return 0;
    }

    private boolean backtrack(Echiquier echiquier, int[] positions, int ligne, int taille, long startTime) {
        if (System.currentTimeMillis() - startTime > TIMEOUT_MS) {
            return false; // Timeout dépassé
        }

        if (ligne == nombreDames) {
            return verifPlacementValide(echiquier, positions);
        }

        for (int col = 0; col < taille; col++) {
            if (positionValide(positions, ligne, col)) {
                positions[ligne] = col;
                if (backtrack(echiquier, positions, ligne + 1, taille, startTime)) {
                    return true;
                }
                positions[ligne] = -1;
            }
        }
        return false;
    }

    private boolean positionValide(int[] positions, int ligneNouvelle, int colonneNouvelle) {
        for (int i = 0; i < ligneNouvelle; i++) {
            int col = positions[i];
            if (col == colonneNouvelle ||
                    col - i == colonneNouvelle - ligneNouvelle ||
                    col + i == colonneNouvelle + ligneNouvelle) {
                return false;
            }
        }
        return true;
    }

    private boolean verifPlacementValide(Echiquier echiquier, int[] positions) {
        for (int ligne = 0; ligne < nombreDames; ligne++) {
            Case c = echiquier.getCase(ligne, positions[ligne]);
            if (c == null || c.isEstVide()) {
                return false;
            }
            Piece p = c.getPiece();
            if (!(p instanceof Dame)) {
                return false;
            }
        }
        return true;
    }
}
