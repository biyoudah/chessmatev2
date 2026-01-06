package fr.univlorraine.pierreludmannchessmate;

import lombok.Getter;
import lombok.Setter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Gère la logique métier du jeu d'échecs et des puzzles.
 */
public class ChessGame {

    private Echiquier echiquier;
    private Piece[][] plateauLogique = new Piece[8][8];

    @Getter
    private Joueur joueur;

    @Getter
    private int erreurs;

    @Getter
    private int placementsValides;

    private boolean aRetire;
    private boolean scoreDejaEnregistre;
    private int scoreBrut;

    @Getter @Setter
    private String modeDeJeu;

    @Getter @Setter
    private boolean traitAuBlanc;

    @Getter @Setter
    private Map<String, Integer> configurationRequise;

    public ChessGame() {
        this.echiquier = new Echiquier();
        this.joueur = new Joueur("Joueur", true);
        this.modeDeJeu = "8-dames";
        this.traitAuBlanc = true;
        this.configurationRequise = new HashMap<>();
        this.configurationRequise.put("Dame", 8);
    }

    public void reinitialiser() {
        echiquier.initialiser();
        this.plateauLogique = new Piece[8][8];
        this.traitAuBlanc = true;
        this.erreurs = 0;
        this.placementsValides = 0;
        this.aRetire = false;
        this.scoreDejaEnregistre = false;
        this.scoreBrut = 0;
    }

    public String placerPiece(int x, int y, String typePiece, boolean estBlanc) {
        Case c = echiquier.getCase(x, y);
        if (!c.isEstVide()) {
            erreurs++;
            return "OCCUPEE";
        }

        if (estCaseMenacee(x, y)) {
            erreurs++;
            return "INVALID";
        }

        Piece piece = creerPiece(typePiece, estBlanc);
        if (piece == null) return "ERREUR";

        echiquier.placerPiece(x, y, piece);
        this.plateauLogique[x][y] = piece;
        placementsValides++;
        scoreBrut += poidsPiece(typePiece);
        return "OK";
    }

    public boolean retirerPiece(int x, int y) {
        Case c = echiquier.getCase(x, y);
        if (c.isEstVide()) return false;
        c.setPiece(null);
        c.setEstVide(true);
        this.plateauLogique[x][y] = null;
        aRetire = true;
        return true;
    }

    public Piece recupererObjetPiece(int x, int y) {
        if (x < 0 || x > 7 || y < 0 || y > 7) return null;
        return plateauLogique[x][y];
    }

    public boolean cheminLibre(int dx, int dy, int ax, int ay) {
        Piece p = recupererObjetPiece(dx, dy);
        if (p instanceof Cavalier) return true;

        int deltaX = Integer.compare(ax, dx);
        int deltaY = Integer.compare(ay, dy);

        int currX = dx + deltaX;
        int currY = dy + deltaY;

        while (currX != ax || currY != ay) {
            if (recupererObjetPiece(currX, currY) != null) return false;
            currX += deltaX;
            currY += deltaY;
        }
        return true;
    }

    private boolean estCaseMenacee(int targetX, int targetY) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Case c = echiquier.getCase(i, j);
                if (!c.isEstVide() && c.getPiece() != null) {
                    String typeAttaquant = c.getPiece().getClass().getSimpleName();
                    int dx = Math.abs(i - targetX);
                    int dy = Math.abs(j - targetY);
                    if (attaque(typeAttaquant, dx, dy)) return true;
                }
            }
        }
        return false;
    }

    public String validerConfiguration(Map<String, Integer> config) {
        int total = 0;
        Map<String, Integer> maxs = Map.of("Dame", 8, "Tour", 8, "Fou", 14, "Cavalier", 32, "Roi", 16, "Pion", 8);
        for (Map.Entry<String, Integer> e : config.entrySet()) {
            if (e.getValue() < 0) return "Négatif interdit";
            int max = maxs.getOrDefault(e.getKey(), 64);
            if (e.getValue() > max) return "Impossible : Max " + max + " " + e.getKey();
            total += e.getValue();
        }
        return total == 0 ? "Choisissez au moins une pièce." : "OK";
    }

    public boolean estPuzzleResolu() {
        Map<String, Integer> compte = getCompteActuelCalculated();
        for (Map.Entry<String, Integer> entry : configurationRequise.entrySet()) {
            if (!compte.getOrDefault(entry.getKey(), 0).equals(entry.getValue())) return false;
        }
        return true;
    }

    private int poidsPiece(String type) {
        return switch (type) {
            case "Dame" -> 5;
            case "Tour" -> 4;
            case "Fou", "Cavalier" -> 3;
            case "Roi" -> 2;
            default -> 1;
        };
    }

    public boolean estTentativeParfaite() {
        return erreurs == 0 && !aRetire;
    }

    public int getScoreCourant() {
        return scoreBrut;
    }

    public int calculerScoreFinalSansBonus() {
        double facteur = Math.max(0.2, 1.0 - 0.1 * erreurs);
        if (estTentativeParfaite()) facteur += 0.2;
        return (int) Math.round(scoreBrut * facteur);
    }

    public boolean estScoreDejaEnregistre() {
        return scoreDejaEnregistre;
    }

    public void marquerScoreEnregistre() {
        this.scoreDejaEnregistre = true;
    }

    private boolean attaque(String type, int dx, int dy) {
        return switch (type) {
            case "Dame" -> (dx == 0 || dy == 0) || (dx == dy);
            case "Tour" -> (dx == 0 || dy == 0);
            case "Fou" -> (dx == dy);
            case "Roi" -> (dx <= 1 && dy <= 1);
            case "Cavalier" -> (dx * dy == 2);
            default -> false;
        };
    }

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

    public Map<String, Integer> getCompteActuel() {
        Map<String, Integer> counts = new HashMap<>();
        if (configurationRequise != null) {
            for (String k : configurationRequise.keySet()) counts.put(k, 0);
        }
        counts.putAll(getCompteActuelCalculated());
        return counts;
    }

    private Map<String, Integer> getCompteActuelCalculated() {
        Map<String, Integer> c = new HashMap<>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (!echiquier.getCase(i, j).isEstVide()) {
                    String t = echiquier.getCase(i, j).getPiece().getClass().getSimpleName();
                    c.put(t, c.getOrDefault(t, 0) + 1);
                }
            }
        }
        return c;
    }

    private Piece creerPiece(String type, boolean estBlanc) {
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

    public void chargerFen(String fen) {
        reinitialiser();
        String disposition = fen.split(" ")[0];
        String[] rangees = disposition.split("/");
        for (int i = 0; i < 8; i++) {
            int col = 0;
            for (char c : rangees[i].toCharArray()) {
                if (Character.isDigit(c)) col += Character.getNumericValue(c);
                else {
                    boolean blanc = Character.isUpperCase(c);
                    String t = switch (Character.toLowerCase(c)) {
                        case 'k' -> "Roi"; case 'q' -> "Dame"; case 'r' -> "Tour";
                        case 'b' -> "Fou"; case 'n' -> "Cavalier"; case 'p' -> "Pion";
                        default -> null;
                    };
                    if (t != null) placerPiece(7 - i, col, blanc); // Simplifié
                    col++;
                }
            }
        }
    }

    private void placerPiece(int x, int y, boolean estBlanc) {
    }
}