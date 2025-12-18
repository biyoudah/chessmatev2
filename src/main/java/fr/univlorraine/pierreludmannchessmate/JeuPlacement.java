package fr.univlorraine.pierreludmannchessmate;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class JeuPlacement extends AbstractChessGame {

    @Getter @Setter
    private Map<String, Integer> configurationRequise;
    @Getter @Setter
    private String modeDeJeu;


    public JeuPlacement() {
        super();
        this.configurationRequise = new HashMap<>();
        this.configurationRequise.put("Dame", 8);
    }

    public String placerPieceJoueur(int x, int y, String typePiece, boolean estBlanc) {
        Case c = echiquier.getCase(x, y);
        if (!c.isEstVide()) return "OCCUPEE";

        // Règle spécifique à ce mode de jeu :
        if (estCaseMenacee(x, y)) {
            return "INVALID";
        }

        Piece p = factoryPiece(typePiece, estBlanc);
        placerPieceInterne(x, y, p); // On appelle la méthode du parent
        return "OK";
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
            if (e.getValue() > max) return "Impossible : Max " + max + " " + e.getKey() + "s";
            total += e.getValue();
        }
        if (total == 0) return "Choisissez au moins une pièce.";
        if (total > 64) return "Impossible : Plus de 64 pièces.";
        return "OK";
    }

    public boolean estPuzzleResolu() {
        return verifierSolution(this.configurationRequise);
    }

    public boolean verifierSolution(Map<String, Integer> configRequise) {
        Map<String, Integer> compte = getCompteActuelCalculated();

        // 1. Vérif quantités exactes
        for (Map.Entry<String, Integer> entry : configRequise.entrySet()) {
            if (!compte.getOrDefault(entry.getKey(), 0).equals(entry.getValue())) return false;
        }

        // 2. Vérif pas de pièces intruses
        long typesRequis = configRequise.values().stream().filter(v -> v > 0).count();
        long typesPresents = compte.values().stream().filter(v -> v > 0).count();

        return typesRequis == typesPresents;
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

}
