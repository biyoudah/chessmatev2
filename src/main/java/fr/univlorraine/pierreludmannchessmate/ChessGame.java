package fr.univlorraine.pierreludmannchessmate;

public class ChessGame {
    private Echiquier echiquier;
    private Joueur joueur;
    private int score;
    private String modeDeJeu; // Ex: "8-queens", "knight-tour", "custom"

    // Constructeur par défaut - échiquier vide
    public ChessGame() {
        this.echiquier = new Echiquier();
        this.joueur = new Joueur("Joueur", true);
        this.score = 0;
        this.modeDeJeu = "custom";
        // NE PAS initialiser les pièces - l'échiquier reste vide
    }

    // Constructeur avec pseudo personnalisé
    public ChessGame(String pseudo) {
        this.echiquier = new Echiquier();
        this.joueur = new Joueur(pseudo, true);
        this.score = 0;
        this.modeDeJeu = "custom";
    }

    // Placer une pièce sur l'échiquier
    public boolean placerPiece(int x, int y, String typePiece, boolean estBlanc) {
        Case caseDestination = echiquier.getCase(x, y);

        // Vérifier que la case est vide
        if (!caseDestination.isEstVide()) {
            return false;
        }

        Piece piece = creerPiece(typePiece, estBlanc);
        if (piece == null) {
            return false;
        }

        echiquier.placerPiece(x, y, piece);
        return true;
    }

    // Créer une pièce selon son type
    private Piece creerPiece(String type, boolean estBlanc) {
        switch (type.toLowerCase()) {
            case "roi":
                return new Roi(estBlanc);
            case "dame":
                return new Dame(estBlanc);
            case "tour":
                return new Tour(estBlanc);
            case "fou":
                return new Fou(estBlanc);
            case "cavalier":
                return new Cavalier(estBlanc);
            case "pion":
                return new Pion(estBlanc);
            default:
                return null;
        }
    }

    // Retirer une pièce
    public boolean retirerPiece(int x, int y) {
        Case caseSource = echiquier.getCase(x, y);
        if (caseSource.isEstVide()) {
            return false;
        }

        caseSource.setPiece(null);
        caseSource.setEstVide(true);
        return true;
    }

    // Déplacer une pièce
    public boolean deplacerPiece(int fromX, int fromY, int toX, int toY) {
        return echiquier.deplacerPiece(fromX, fromY, toX, toY);
    }

    // Réinitialiser l'échiquier (tout vider)
    public void reinitialiser() {
        echiquier.initialiser();
        score = 0;
    }

    // Obtenir la représentation du plateau
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

    // Compter le nombre de pièces sur le plateau
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

    // Vérifier si deux pièces s'attaquent (pour le problème des 8 reines par exemple)
    public boolean verifierConflits() {
        // À implémenter selon ton mode de jeu
        return false;
    }

    // Getters
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
