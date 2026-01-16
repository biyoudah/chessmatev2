package fr.univlorraine.pierreludmannchessmate.logic;

import fr.univlorraine.pierreludmannchessmate.model.*;
import lombok.Getter;
import lombok.Setter;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe de base abstraite pour les modes de jeu d'échecs.
 * <p>
 * Gère un échiquier logique et visuel, le trait, le score et expose des
 * opérations communes telles que le chargement FEN, la réinitialisation,
 * la récupération d'une représentation pour l'interface et le placement/retrait
 * de pièces. Les classes concrètes (ex. {@link JeuPuzzle}) spécialisent la
 * logique de réussite via {@link #estPuzzleResolu()}.
 */
public abstract class AbstractChessGame {

    protected Echiquier echiquier;

    protected Piece[][] plateauLogique = new Piece[8][8];

    @Getter @Setter
    protected Joueur joueur;

    @Getter @Setter
    protected int score = 0;

    @Getter @Setter
    protected boolean traitAuBlanc = true;

    /**
     * Construit une nouvelle instance avec un échiquier initialisé, un joueur
     * par défaut et un plateau logique vide.
     */
    public AbstractChessGame() {
        this.echiquier = new Echiquier();
        this.joueur = new Joueur("Joueur", true);
        reinitialiser();
    }

    /**
     * Indique si l'objectif du mode de jeu est atteint.
     * @return {@code true} si la condition de victoire est remplie
     */
    public abstract boolean estPuzzleResolu();



    /**
     * Réinitialise l'état du jeu: échiquier, plateau logique, trait et score.
     */
    public void reinitialiser() {
        echiquier.initialiser();
        this.plateauLogique = new Piece[8][8];
        this.traitAuBlanc = true;
        this.score = 0;
    }

    /**
     * Retourne la pièce logique située aux coordonnées données.
     * @param x colonne (0..7)
     * @param y ligne (0..7)
     * @return la pièce, ou {@code null} si hors limites ou vide
     */
    public Piece getPieceObject(int x, int y) {
        if (x < 0 || x > 7 || y < 0 || y > 7) return null;
        return plateauLogique[x][y];
    }

    /**
     * Retourne la représentation visuelle pour l'interface graphique.
     * CORRECTION MAJEURE ICI : Inversion [x][y] vers [y][x].
     * @return une matrice 8x8 de chaînes représentant chaque case
     */
    public String[][] getBoard() {
        String[][] board = new String[8][8];

        for (int x = 0; x < 8; x++) {     // x = Colonne (0..7)
            for (int y = 0; y < 8; y++) { // y = Ligne (0..7)

                Case c = echiquier.getCase(x, y);

                board[y][x] = (!c.isEstVide() && c.getPiece() != null) ? c.getPiece().dessiner() : "";
            }
        }
        return board;
    }

    /**
     * Retire une pièce de l'échiquier aux coordonnées données.
     * @param x colonne (0..7)
     * @param y ligne (0..7)
     * @return {@code true} si une pièce a été retirée, sinon {@code false}
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
     * Place une pièce sur l'échiquier et met à jour le plateau logique.
     * @param x colonne (0..7)
     * @param y ligne (0..7)
     * @param piece instance de pièce à placer
     */
    protected void placerPieceInterne(int x, int y, Piece piece) {
        if (piece == null) return;
        echiquier.placerPiece(x, y, piece);
        this.plateauLogique[x][y] = piece;
    }

    /**
     * Calcule le nombre d'occurrences par type de pièce actuellement placée.
     * @return une map {TypeDePieceSimpleName -> quantité}
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
     * Cette méthode est correcte mathématiquement (x=col, y=7-i).
     * Avec le correctif de getBoard() ci-dessus, l'affichage sera bon.
     * @param fen chaîne FEN (ex: "8/8/8/8/8/8/8/8 w - - 0 1")
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


    /**
     * Fabrique une pièce à partir d'un libellé de type et d'une couleur.
     * @param type libellé de pièce (Roi, Dame, Tour, Fou, Cavalier, Pion)
     * @param estBlanc {@code true} si la pièce est blanche
     * @return la pièce correspondante, ou {@code null} si type inconnu
     */
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

    /**
     * Convertit un caractère FEN en libellé de type de pièce.
     * @param c caractère FEN (k,q,r,b,n,p)
     * @return libellé (Roi, Dame, Tour, Fou, Cavalier, Pion) ou {@code null}
     */
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