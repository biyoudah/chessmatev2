package fr.univlorraine.pierreludmannchessmate;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe principale représentant une partie d'échecs.
 * Cette classe gère la logique du jeu, notamment le placement des pièces,
 * la vérification des règles (pièces qui s'attaquent), et la validation
 * des configurations de puzzle (comme le problème des 8 dames).
 * Elle maintient l'état du plateau et les règles du mode de jeu actuel.
 */
public class ChessGame {

    private Echiquier echiquier;

    @Getter
    private Joueur joueur;

    @Getter @Setter
    private int score;

    @Getter @Setter
    private String modeDeJeu;

    @Getter @Setter
    private boolean traitAuBlanc;

    @Getter @Setter
    private Map<String, Integer> configurationRequise;

    // --- CONSTRUCTEURS ---

    /**
     * Constructeur par défaut.
     * Initialise une nouvelle partie avec un échiquier vide, un joueur par défaut,
     * et le mode de jeu "8-dames" (problème des 8 dames).
     */
    public ChessGame() {
        this.echiquier = new Echiquier();
        this.joueur = new Joueur("Joueur", true);
        this.score = 0;
        this.modeDeJeu = "8-dames"; // Par défaut
        this.traitAuBlanc = true;
        this.configurationRequise = new HashMap<>();
        this.configurationRequise.put("Dame", 8);
    }

    /**
     * Constructeur avec pseudo du joueur.
     * Initialise une nouvelle partie avec un échiquier vide et le pseudo spécifié.
     * 
     * @param pseudo Le pseudo du joueur
     */
    public ChessGame(String pseudo) {
        this();
        this.joueur = new Joueur(pseudo, true);
    }

    // --- ACTIONS JEU ---

    /**
     * Réinitialise la partie.
     * Vide l'échiquier, remet le score à zéro et donne le trait aux blancs.
     */
    public void reinitialiser() {
        echiquier.initialiser();
        score = 0;
        traitAuBlanc = true;
    }

    /**
     * Place une pièce sur l'échiquier.
     * Vérifie si la case est vide et si la pièce ne serait pas menacée par une autre pièce.
     * 
     * @param x Coordonnée X de la case (0-7)
     * @param y Coordonnée Y de la case (0-7)
     * @param typePiece Type de pièce à placer (Dame, Tour, Fou, etc.)
     * @param estBlanc Indique si la pièce est blanche (true) ou noire (false)
     * @return Code de résultat: "OK" si réussi, "OCCUPEE" si case occupée, "INVALID" si case menacée, "ERREUR" si type invalide
     */
    public String placerPiece(int x, int y, String typePiece, boolean estBlanc) {
        Case c = echiquier.getCase(x, y);
        if (!c.isEstVide()) return "OCCUPEE";

        // Vérification immédiate : Est-ce que je pose la pièce sur une case attaquée ?
        if (estCaseMenacee(x, y)) {
            return "INVALID";
        }

        Piece piece = creerPiece(typePiece, estBlanc);
        if (piece == null) return "ERREUR";

        echiquier.placerPiece(x, y, piece);
        return "OK";
    }

    /**
     * Retire une pièce de l'échiquier.
     * 
     * @param x Coordonnée X de la case (0-7)
     * @param y Coordonnée Y de la case (0-7)
     * @return true si une pièce a été retirée, false si la case était déjà vide
     */
    public boolean retirerPiece(int x, int y) {
        Case c = echiquier.getCase(x, y);
        if (c.isEstVide()) return false;
        c.setPiece(null);
        c.setEstVide(true);
        return true;
    }

    /**
     * Vérifie si la case (targetX, targetY) est attaquée par une pièce déjà présente.
     * Parcourt toutes les pièces sur l'échiquier et vérifie si l'une d'elles peut
     * attaquer la case cible selon les règles de déplacement des pièces d'échecs.
     * 
     * @param targetX Coordonnée X de la case cible (0-7)
     * @param targetY Coordonnée Y de la case cible (0-7)
     * @return true si la case est menacée par au moins une pièce, false sinon
     */
    private boolean estCaseMenacee(int targetX, int targetY) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Case c = echiquier.getCase(i, j);
                if (!c.isEstVide() && c.getPiece() != null) {
                    // La pièce en (i,j) attaque-t-elle (targetX, targetY) ?
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
     * Vérifie si la configuration demandée par l'utilisateur est théoriquement possible.
     * Contrôle le nombre de pièces de chaque type et le nombre total de pièces.
     * 
     * @param config Map contenant le nombre de pièces par type
     * @return "OK" si la configuration est valide, sinon un message d'erreur explicatif
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

    /**
     * Vérifie si le puzzle actuel est résolu.
     * Un puzzle est résolu lorsque toutes les pièces requises sont placées
     * sur l'échiquier sans qu'aucune ne soit menacée par une autre.
     * 
     * @return true si le puzzle est résolu, false sinon
     */
    public boolean estPuzzleResolu() {
        return verifierSolution(this.configurationRequise);
    }

    /**
     * Vérifie si la configuration actuelle de l'échiquier correspond à la configuration requise.
     * Contrôle que le nombre exact de pièces de chaque type est présent et qu'il n'y a pas de pièces supplémentaires.
     * 
     * @param configRequise Map contenant le nombre de pièces requis par type
     * @return true si la configuration actuelle correspond à la configuration requise, false sinon
     */
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
        // Note : Les conflits sont déjà gérés par "placerPiece" qui empêche de poser si menacé.
    }

    /**
     * Détermine si une pièce d'un type donné peut attaquer une case à une distance donnée.
     * Implémente les règles de déplacement des différentes pièces d'échecs.
     * 
     * @param type Type de la pièce (Dame, Tour, Fou, Roi, Cavalier)
     * @param dx Distance horizontale absolue entre la pièce et la case cible
     * @param dy Distance verticale absolue entre la pièce et la case cible
     * @return true si la pièce peut attaquer la case, false sinon
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

    // --- UTILITAIRES ---

    /**
     * Retourne une représentation du plateau sous forme de tableau 2D de chaînes.
     * Chaque case contient soit une chaîne vide (case vide), soit la représentation
     * de la pièce qui s'y trouve.
     * 
     * @return Tableau 2D 8x8 représentant l'état actuel du plateau
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
     * Retourne le décompte actuel des pièces sur l'échiquier.
     * Initialise le compteur avec toutes les pièces requises à 0,
     * puis ajoute le compte réel des pièces présentes.
     * 
     * @return Map contenant le nombre de pièces de chaque type présentes sur l'échiquier
     */
    public Map<String, Integer> getCompteActuel() {
        Map<String, Integer> counts = new HashMap<>();
        if (configurationRequise != null) {
            for (String k : configurationRequise.keySet()) counts.put(k, 0);
        }
        counts.putAll(getCompteActuelCalculated());
        return counts;
    }

    /**
     * Calcule le nombre actuel de pièces de chaque type sur l'échiquier.
     * Parcourt toutes les cases de l'échiquier et compte les pièces par type.
     * 
     * @return Map contenant le nombre de pièces de chaque type présentes sur l'échiquier
     */
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

    /**
     * Crée une nouvelle pièce du type spécifié.
     * 
     * @param type Type de la pièce à créer (roi, dame, tour, fou, cavalier, pion)
     * @param estBlanc Indique si la pièce est blanche (true) ou noire (false)
     * @return La pièce créée, ou null si le type est invalide
     */
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
}
