package fr.univlorraine.pierreludmannchessmate.logic;

import fr.univlorraine.pierreludmannchessmate.model.*;
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

    // --- Méthodes Abstraites ---
    public abstract boolean estPuzzleResolu();


    // --- Méthodes Concrètes ---

    public void reinitialiser() {
        echiquier.initialiser();
        this.plateauLogique = new Piece[8][8];
        this.traitAuBlanc = true;
        this.score = 0;
    }

    public Piece getPieceObject(int x, int y) {
        if (x < 0 || x > 7 || y < 0 || y > 7) return null;
        return plateauLogique[x][y];
    }

    /**
     * Retourne la représentation visuelle pour l'interface graphique.
     * CORRECTION MAJEURE ICI : Inversion [x][y] vers [y][x].
     */
    public String[][] getBoard() {
        // Le tableau doit être [LIGNE][COLONNE] pour le HTML
        String[][] board = new String[8][8];

        for (int x = 0; x < 8; x++) {     // x = Colonne (0..7)
            for (int y = 0; y < 8; y++) { // y = Ligne (0..7)

                // On récupère la case logique à (x, y)
                Case c = echiquier.getCase(x, y);

                // On remplit le tableau visuel à [y][x] (Ligne, Colonne)
                board[y][x] = (!c.isEstVide() && c.getPiece() != null) ? c.getPiece().dessiner() : "";
            }
        }
        return board;
    }

    public boolean retirerPiece(int x, int y) {
        Case c = echiquier.getCase(x, y);
        if (c.isEstVide()) return false;

        c.setPiece(null);
        c.setEstVide(true);
        this.plateauLogique[x][y] = null;
        return true;
    }

    protected void placerPieceInterne(int x, int y, Piece piece) {
        if (piece == null) return;
        echiquier.placerPiece(x, y, piece);
        this.plateauLogique[x][y] = piece;
    }

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
     * Cette méthode est correcte mathématiquement (x=col, y=7-i).
     * Avec le correctif de getBoard() ci-dessus, l'affichage sera bon.
     */
    public void chargerFen(String fen) {
        reinitialiser();

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
                        // i correspond à la ligne visuelle du haut vers le bas (FEN order)
                        // Donc y = 7 - i
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

    // --- Helpers ---

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