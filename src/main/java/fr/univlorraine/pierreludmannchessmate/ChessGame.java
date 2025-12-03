package fr.univlorraine.pierreludmannchessmate;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

public class ChessGame {
    private Echiquier echiquier;
    @Getter
    private Joueur joueur;
    @Setter
    @Getter
    private int score;
    @Setter
    @Getter
    private String modeDeJeu; // "8-queens", "custom", etc.

    public ChessGame() {
        this.echiquier = new Echiquier();
        this.joueur = new Joueur("Joueur", true);
        this.score = 0;
        this.modeDeJeu = "custom";
    }

    public ChessGame(String pseudo) {
        this.echiquier = new Echiquier();
        this.joueur = new Joueur(pseudo, true);
        this.score = 0;
        this.modeDeJeu = "custom";
    }

    /**
     * Tente de placer une pièce.
     * Retourne un code statut : "OK", "OCCUPEE", "INVALID" (conflit), ou "ERREUR".
     */
    public String placerPiece(int x, int y, String typePiece, boolean estBlanc) {
        Case caseDestination = echiquier.getCase(x, y);

        // 1. Vérifier si la case est déjà occupée
        if (!caseDestination.isEstVide()) {
            return "OCCUPEE";
        }

        // 2. Vérifier les conflits (Si une reine attaque cette position)
        if (estEnConflit(x, y)) {
            return "INVALID";
        }

        if(estMenacant(x,y,typePiece,estBlanc)){
            return "MENACANT";
        }

        // 3. Création de la pièce
        Piece piece = creerPiece(typePiece, estBlanc);
        if (piece == null) {
            return "ERREUR";
        }

        // 4. Placement effectif
        echiquier.placerPiece(x, y, piece);
        return "OK";
    }

    /**
     * Vérifie si la position (x, y) est attaquée par une pièce déjà sur le plateau.
     * (Logique simplifiée pour les Dames/Reines : Ligne, Colonne, Diagonale)
     */
    /**
     * Vérifie si la position cible (x, y) est menacée par une pièce existante.
     * Utilise le polymorphisme : c'est la pièce elle-même qui calcule sa portée.
     */
    private boolean estEnConflit(int x, int y) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Case c = echiquier.getCase(i, j);

                // S'il y a une pièce sur cette case
                if (!c.isEstVide() && c.getPiece() != null) {
                    Piece pieceExistante = c.getPiece();

                    // On demande à la pièce spécifique (Cavalier, Dame, etc.)
                    // si elle peut atteindre la case cible (x, y) depuis sa position (i, j).
                    if (pieceExistante.deplacementValide(i, j, x, y)) {
                        return true; // Conflit détecté !
                    }
                }
            }
        }
        return false;
    }

    private boolean estMenacant(int x, int y, String  typePiece, boolean estBlanc) {
       Piece newPiece = creerPiece(typePiece, estBlanc);

       for (int i = 0; i < 8; i++) {
           for (int j = 0; j < 8; j++) {
               Case c = echiquier.getCase(i, j);
               if (!c.isEstVide() && c.getPiece() != null) {

                   // Vérifie si la nouvelle pièce peut menacer cette pièce existante
                   if (newPiece.deplacementValide(x, y, i, j)) {
                       return true;
                   }
               }
           }
       }
               return false;
    }

    /**
     * Retire une pièce du plateau.
     * Retourne true si une pièce a été retirée, false si la case était déjà vide.
     */
    public boolean retirerPiece(int x, int y) {
        Case caseSource = echiquier.getCase(x, y);

        if (caseSource.isEstVide()) {
            return false; // Rien à retirer
        }

        caseSource.setPiece(null);
        caseSource.setEstVide(true);
        return true;
    }

    /**
     * Vérifie si la condition de victoire est atteinte.
     */
    public boolean estPuzzleResolu() {
        // On utilise la logique 8 reines par défaut ou si le mode est spécifié
        // Ici je force la vérification pour que tu puisses gagner en mode "custom" aussi si tu veux
        return verifier8Reines();
    }

    private boolean verifier8Reines() {
        int n = 8;
        int countDames = 0;

        // Compter les dames
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (!echiquier.getCase(i, j).isEstVide()) {
                    countDames++;
                }
            }
        }

        // S'il n'y a pas 8 dames, ce n'est pas fini
        if (countDames != 8) return false;

        // S'il y a 8 dames, comme la méthode 'placerPiece' empêche déjà les conflits,
        // alors c'est forcément GAGNÉ !
        return true;
    }

    public void reinitialiser() {
        echiquier.initialiser();
        score = 0;
    }

    // --- Méthodes utilitaires et Getters/Setters ---

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
                if (!echiquier.getCase(i, j).isEstVide()) count++;
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

}