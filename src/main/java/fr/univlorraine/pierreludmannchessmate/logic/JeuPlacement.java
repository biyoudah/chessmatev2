package fr.univlorraine.pierreludmannchessmate.logic;

import fr.univlorraine.pierreludmannchessmate.model.Case;
import fr.univlorraine.pierreludmannchessmate.model.Piece;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Gère la logique du mode "Placement" (ex: Problème des 8 dames).
 * Centralise le calcul du score et le suivi des erreurs de l'utilisateur.
 */
public class JeuPlacement extends AbstractChessGame {

    @Getter @Setter
    private Map<String, Integer> configurationRequise;

    @Getter @Setter
    private String modeDeJeu;

    @Getter @Setter
    private boolean scoreEnregistre = false;

    // --- Suivi de la performance du joueur ---
    @Getter
    private int erreurs = 0;

    public JeuPlacement() {
        super();
        this.configurationRequise = new HashMap<>();
        // Configuration par défaut (8 Dames)
        this.configurationRequise.put("Dame", 8);
    }

    /**
     * Tente de placer une pièce sur l'échiquier.
     * Si la case est menacée par une autre pièce, le placement échoue et est considéré comme une erreur.
     */
    public String placerPieceJoueur(int x, int y, String typePiece, boolean estBlanc) {
        Case c = echiquier.getCase(x, y);
        if (!c.isEstVide()) return "OCCUPEE";

        // Règle d'or du mode placement : pas de pièce sur une case menacée
        if (estCaseMenacee(x, y)) {
            return "INVALID"; // Le contrôleur appellera incrementerErreurs() suite à ce retour
        }

        Piece p = factoryPiece(typePiece, estBlanc);
        placerPieceInterne(x, y, p);
        return "OK";
    }

    /**
     * Incrémente le compteur d'erreurs pour la partie en cours.
     */
    public void incrementerErreurs() {
        this.erreurs++;
    }

    /**
     * Calcule le score courant basé sur la complexité et les erreurs.
     * Formule : 100 (base) + (5 pts par pièce requise) - (5 pts par erreur).
     */
    public int getScoreCourant() {
        int base = 100;
        int nbPiecesRequises = configurationRequise.values().stream().mapToInt(Integer::intValue).sum();

        int total = base + (nbPiecesRequises * 5) - (erreurs * 5);
        return Math.max(0, total); // Le score ne peut pas être négatif
    }

    /**
     * Indique si le joueur a réussi le défi sans commettre d'erreur.
     */
    public boolean estTentativeParfaite() {
        return erreurs == 0;
    }

    /**
     * Réinitialise complètement le plateau et les statistiques de la session de jeu.
     */
    @Override
    public void reinitialiser() {
        super.reinitialiser(); // Vide le plateau via AbstractChessGame
        this.erreurs = 0;
        this.scoreEnregistre = false;
    }

    /**
     * Vérifie si l'état actuel de l'échiquier correspond aux objectifs fixés.
     */
    public boolean estPuzzleResolu() {
        return verifierSolution(this.configurationRequise);
    }

    private boolean verifierSolution(Map<String, Integer> configRequise) {
        Map<String, Integer> compte = getCompteActuelCalculated();

        // 1. Vérification des quantités pour chaque type de pièce requis
        for (Map.Entry<String, Integer> entry : configRequise.entrySet()) {
            if (!compte.getOrDefault(entry.getKey(), 0).equals(entry.getValue())) return false;
        }

        // 2. Vérification de l'absence de types de pièces non demandés
        long typesRequis = configRequise.values().stream().filter(v -> v > 0).count();
        long typesPresents = compte.values().stream().filter(v -> v > 0).count();

        return typesRequis == typesPresents;
    }

    /**
     * Parcourt l'échiquier pour déterminer si la case cible est attaquée par une pièce présente.
     */
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

    /**
     * Logique de mouvement simplifiée pour détecter les menaces.
     */
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

    /**
     * Valide si une configuration personnalisée est techniquement possible.
     */
    public String validerConfiguration(Map<String, Integer> config) {
        int total = 0;
        Map<String, Integer> maxs = Map.of("Dame", 8, "Tour", 8, "Fou", 14, "Cavalier", 32, "Roi", 16, "Pion", 8);

        for (Map.Entry<String, Integer> e : config.entrySet()) {
            if (e.getValue() < 0) return "Négatif interdit";
            int max = maxs.getOrDefault(e.getKey(), 64);
            if (e.getValue() > max) return "Impossible : Max " + max + " " + e.getKey() + "s";
            total += e.getValue();
        }
        if (total == 0) return "Choisissez au moins une pièce.";
        if (total > 64) return "Impossible : Plus de 64 pièces.";
        return "OK";
    }
}