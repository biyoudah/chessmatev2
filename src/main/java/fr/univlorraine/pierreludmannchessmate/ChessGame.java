package fr.univlorraine.pierreludmannchessmate;

import java.util.Arrays;

public class ChessGame {
    private Echiquier echiquier;
    private Joueur joueur;
    private int score;
    private String modeDeJeu; // Par exemple "8-queens", "custom", etc.

    public ChessGame() {
        this.echiquier = new Echiquier();
        this.joueur = new Joueur("Joueur", true);
        this.score = 0;
        this.modeDeJeu = "custom"; // Par défaut mode personnalisé
    }

    public ChessGame(String pseudo) {
        this.echiquier = new Echiquier();
        this.joueur = new Joueur(pseudo, true);
        this.score = 0;
        this.modeDeJeu = "custom";
    }

    public boolean placerPiece(int x, int y, String typePiece, boolean estBlanc) {
        Case caseDestination = echiquier.getCase(x, y);

        if (!caseDestination.isEstVide()) {
            return false; // Case déjà occupée
        }

        Piece piece = creerPiece(typePiece, estBlanc);
        if (piece == null) {
            return false; // Type de pièce inconnu
        }

        echiquier.placerPiece(x, y, piece);

        if ("8-queens".equals(modeDeJeu)) {
            return validerSolution8Reines();
        }

        return true;
    }

    // Validation simple du problème des 8 reines
    public boolean validerSolution8Reines() {
        int n = 8;
        int[] positions = new int[n];
        Arrays.fill(positions, -1);

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                Case c = echiquier.getCase(i, j);
                Piece p = c.getPiece();
                if (p instanceof Dame) {
                    positions[i] = j;
                    break;
                }
            }
        }

        for (int i = 0; i < n; i++) {
            if (positions[i] == -1) return false; // Pas assez de dames

            for (int j = i + 1; j < n; j++) {
                if (positions[j] == -1) return false;

                if (positions[i] == positions[j] || Math.abs(positions[i] - positions[j]) == Math.abs(i - j)) {
                    return false; // Conflit détecté
                }
            }
        }
        return true; // Configuration valide
    }

    public boolean retirerPiece(int x, int y) {
        Case caseSource = echiquier.getCase(x, y);
        if (caseSource.isEstVide()) {
            return false; // Case déjà vide
        }

        caseSource.setPiece(null);
        caseSource.setEstVide(true);
        return true;
    }

    public void reinitialiser() {
        echiquier.initialiser();
        score = 0;
    }

    public String[][] getBoard() {
        String[][] board = new String[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Case c = echiquier.getCase(i, j);
                if (c.isEstVide() || c.getPiece() == null) {
                    board[i][j] = "";
                } else {
                    board[i][j] = c.getPiece().dessiner();
                }
            }
        }
        return board;
    }

    public int compterPieces() {
        int count = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (!echiquier.getCase(i, j).isEstVide()) {
                    count++;
                }
            }
        }
        return count;
    }

    private Piece creerPiece(String type, boolean estBlanc) {
        switch (type.toLowerCase()) {
            case "roi": return new Roi(estBlanc);
            case "dame": return new Dame(estBlanc);
            case "tour": return new Tour(estBlanc);
            case "fou": return new Fou(estBlanc);
            case "cavalier": return new Cavalier(estBlanc);
            case "pion": return new Pion(estBlanc);
            default: return null;
        }
    }

    public Joueur getJoueur() {
        return joueur;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getModeDeJeu() {
        return modeDeJeu;
    }

    public void setModeDeJeu(String modeDeJeu) {
        this.modeDeJeu = modeDeJeu;
    }
}
