package fr.univlorraine.pierreludmannchessmate.logic;

import fr.univlorraine.pierreludmannchessmate.model.Case;
import fr.univlorraine.pierreludmannchessmate.model.Piece;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class JeuPlacement extends AbstractChessGame {
    @Getter @Setter
    private String typeSelectionne = "Dame";

    @Getter @Setter
    private Map<String, Integer> configurationRequise;

    @Getter @Setter
    private String modeDeJeu = "8-dames";

    @Getter @Setter
    private boolean scoreEnregistre = false;

    // --- Attributs de l'ancienne version pour le score ---
    @Getter private int erreurs = 0;
    @Getter private int placementsValides = 0;
    @Getter private int scoreBrut = 0;
    private boolean aRetire = false; // Pour le calcul du "Perfect"

    public JeuPlacement() {
        super();
        this.configurationRequise = new HashMap<>();
        this.configurationRequise.put("Dame", 8);
    }

    @Override
    public void reinitialiser() {
        super.reinitialiser();
        this.erreurs = 0;
        this.placementsValides = 0;
        this.scoreBrut = 0;
        this.aRetire = false;
        this.scoreEnregistre = false;
    }

    /**
     * Logique de placement de l'ancienne version adaptée.
     */
    public String placerPieceJoueur(int x, int y, String typePiece, boolean estBlanc) {
        Case c = echiquier.getCase(x, y);

        if (!c.isEstVide()) {
            this.erreurs++;
            return "OCCUPEE";
        }

        // --- MODIFICATION : DOUBLE VÉRIFICATION ---
        // 1. La case est-elle menacée par une pièce du plateau ?
        // 2. La nouvelle pièce va-t-elle menacer une pièce du plateau ?
        if (estCaseMenacee(x, y) || nouvellePieceMenace(x, y, typePiece)) {
            this.erreurs++;
            return "INVALID";
        }

        Piece p = factoryPiece(typePiece, estBlanc);
        if (p == null) return "ERREUR";

        placerPieceInterne(x, y, p);
        this.placementsValides++;
        this.scoreBrut += poidsPiece(typePiece);
        return "OK";
    }

    /**
     * Vérifie si poser un type de pièce en (targetX, targetY) menace une pièce existante.
     */
    private boolean nouvellePieceMenace(int targetX, int targetY, String typeNouvellePiece) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Case c = echiquier.getCase(i, j);
                if (!c.isEstVide() && c.getPiece() != null) {
                    int dx = Math.abs(i - targetX);
                    int dy = Math.abs(j - targetY);
                    if (attaque(typeNouvellePiece, dx, dy)) return true;
                }
            }
        }
        return false;
    }

    public boolean[][] getMatriceMenaces() {
        boolean[][] menaces = new boolean[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                // On affiche les cases rouges selon la pièce que le joueur a en main
                menaces[i][j] = estCaseMenacee(i, j) || nouvellePieceMenace(i, j, this.typeSelectionne);
            }
        }
        return menaces;
    }

    @Override
    public boolean retirerPiece(int x, int y) {
        boolean reussite = super.retirerPiece(x, y);
        if (reussite) {
            this.aRetire = true;
        }
        return reussite;
    }

    // --- Logique de score de l'ancienne version ---

    private int poidsPiece(String type) {
        return switch (type) {
            case "Dame" -> 5;
            case "Tour" -> 4;
            case "Fou", "Cavalier" -> 3;
            case "Roi" -> 2;
            default -> 1;
        };
    }

    public int getScoreCourant() {
        return scoreBrut;
    }

    public int calculerScoreFinalSansBonus() {
        double facteur = Math.max(0.2, 1.0 - 0.1 * erreurs);
        if (estTentativeParfaite()) facteur += 0.2;
        return (int) Math.round(scoreBrut * facteur);
    }

    public boolean estTentativeParfaite() {
        return erreurs == 0 && !aRetire;
    }

    @Override
    public boolean estPuzzleResolu() {
        Map<String, Integer> compte = getCompteActuelCalculated();
        for (Map.Entry<String, Integer> entry : configurationRequise.entrySet()) {
            if (!compte.getOrDefault(entry.getKey(), 0).equals(entry.getValue())) return false;
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

    public String validerConfiguration(Map<String, Integer> config) {
        int total = 0;
        for (int val : config.values()) total += val;
        if (total == 0) return "Choisissez au moins une pièce.";
        if (total > 64) return "Trop de pièces !";
        return "OK";
    }
}