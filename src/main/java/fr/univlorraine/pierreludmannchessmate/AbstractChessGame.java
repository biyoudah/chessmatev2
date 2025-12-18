package fr.univlorraine.pierreludmannchessmate;

import lombok.Getter;
import lombok.Setter;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractChessGame {

    protected Echiquier echiquier;

    // Le plateau logique est protected pour que les enfants puissent l'utiliser si besoin
    protected Piece[][] plateauLogique = new Piece[8][8];

    // --- Attributs Communs ---
    @Getter @Setter
    protected Joueur joueur;

    @Getter @Setter
    protected int score = 0;

    @Getter @Setter
    protected boolean traitAuBlanc = true;

    // --- Constructeur ---
    public AbstractChessGame() {
        this.echiquier = new Echiquier();
        this.joueur = new Joueur("Joueur", true);
        reinitialiser();
    }

    // --- Méthodes Abstraites (Le contrat que les enfants doivent remplir) ---
    /**
     * Chaque mode de jeu définit sa propre condition de victoire.
     */
    public abstract boolean estPuzzleResolu();


    // --- Méthodes Concrètes (Partagées par tout le monde) ---

    public void reinitialiser() {
        echiquier.initialiser();
        this.plateauLogique = new Piece[8][8];
        this.traitAuBlanc = true;
        this.score = 0;
    }

    /**
     * Retourne la pièce logique (Utile pour vérifier les règles ou le type).
     */
    public Piece getPieceObject(int x, int y) {
        if (x < 0 || x > 7 || y < 0 || y > 7) return null;
        return plateauLogique[x][y];
    }

    /**
     * Retourne la représentation visuelle pour l'interface graphique.
     */
    public String[][] getBoard() {
        String[][] board = new String[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Case c = echiquier.getCase(i, j);
                board[i][j] = (!c.isEstVide() && c.getPiece() != null) ? c.getPiece().dessiner() : "";
            }
        }
        return board;
    }

    /**
     * Retire une pièce (Action physique de base).
     */
    public boolean retirerPiece(int x, int y) {
        Case c = echiquier.getCase(x, y);
        if (c.isEstVide()) return false;

        c.setPiece(null);
        c.setEstVide(true);
        this.plateauLogique[x][y] = null;
        return true;
    }

    /**
     * Place une pièce physiquement sans vérifier les règles du jeu.
     * (Utilisé par le setup, le chargement FEN, ou l'IA).
     */
    protected void placerPieceInterne(int x, int y, Piece piece) {
        if (piece == null) return;
        echiquier.placerPiece(x, y, piece);
        this.plateauLogique[x][y] = piece;
    }

    /**
     * Compte les pièces pour la validation (Utilisé par JeuPlacement et potentiellement d'autres).
     */
    public Map<String, Integer> getCompteActuelCalculated() {
        Map<String, Integer> c = new HashMap<>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (plateauLogique[i][j] != null) {
                    String t = plateauLogique[i][j].getClass().getSimpleName();
                    c.put(t, c.getOrDefault(t, 0) + 1);
                }
            }
        }
        return c;
    }

    /**
     * Charge une position FEN.
     */
    public void chargerFen(String fen) {
        reinitialiser(); // On vide tout d'abord

        String[] parties = fen.split(" ");
        String disposition = parties[0];
        String[] rangees = disposition.split("/");

        for (int i = 0; i < 8; i++) {
            String rangee = rangees[i];
            int col = 0;

            for (char c : rangee.toCharArray()) {
                if (Character.isDigit(c)) {
                    col += Character.getNumericValue(c);
                } else {
                    boolean estBlanc = Character.isUpperCase(c);
                    String type = getTypeFromChar(c);

                    if (type != null) {
                        // i=0 est la rangée 8 (index 7), i=7 est la rangée 1 (index 0)
                        placerPieceInterne(col, 7 - i, factoryPiece(type, estBlanc));
                    }
                    col++;
                }
            }
        }

        if (parties.length > 1) {
            this.traitAuBlanc = parties[1].equals("w");
        }
    }

    // --- Helpers (Usines et conversion) ---

    protected Piece factoryPiece(String type, boolean estBlanc) {
        return switch (type.toLowerCase()) {
            case "roi" -> new Roi(estBlanc);
            case "dame" -> new Dame(estBlanc);
            case "tour" -> new Tour(estBlanc);
            case "fou" -> new Fou(estBlanc);
            case "cavalier" -> new Cavalier(estBlanc);
            case "pion" -> new Pion(estBlanc);
            default -> null;
        };
    }

    private String getTypeFromChar(char c) {
        return switch (Character.toLowerCase(c)) {
            case 'k' -> "Roi";
            case 'q' -> "Dame";
            case 'r' -> "Tour";
            case 'b' -> "Fou";
            case 'n' -> "Cavalier";
            case 'p' -> "Pion";
            default -> null;
        };
    }
}