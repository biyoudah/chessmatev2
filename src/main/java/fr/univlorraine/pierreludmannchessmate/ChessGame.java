package fr.univlorraine.pierreludmannchessmate;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe principale représentant le modèle d'une partie d'échecs.
 * <p>
 * Cette classe gère la logique métier du jeu :
 * <ul>
 * <li>Le stockage de l'état du plateau (visuel et logique).</li>
 * <li>Le placement et le retrait des pièces.</li>
 * <li>La validation des règles de déplacement (chemins, menaces).</li>
 * <li>Le chargement de positions via la notation FEN.</li>
 * </ul>
 */
public class ChessGame {

    private Echiquier echiquier;

    /**
     * Plateau logique stockant les instances réelles des objets {@link Piece}.
     * Utilisé pour la validation des règles de déplacement.
     */
    private Piece[][] plateauLogique = new Piece[8][8];

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

    /**
     * Constructeur par défaut.
     * Initialise une nouvelle partie avec un échiquier vide, un joueur par défaut,
     * et le mode de jeu "8-dames".
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
     * @param pseudo Le pseudo du joueur.
     */
    public ChessGame(String pseudo) {
        this();
        this.joueur = new Joueur(pseudo, true);
    }

    /**
     * Réinitialise la partie.
     * Vide l'échiquier visuel et logique, remet le score à zéro et donne le trait aux blancs.
     */
    public void reinitialiser() {
        echiquier.initialiser();
        this.plateauLogique = new Piece[8][8]; // Reset logique
        score = 0;
        traitAuBlanc = true;
    }

    /**
     * Place une pièce sur l'échiquier.
     * <p>
     * Met à jour à la fois l'échiquier visuel (pour l'affichage) et le plateau logique
     * (pour la validation des règles).
     *
     * @param x         Coordonnée X de la case (0-7).
     * @param y         Coordonnée Y de la case (0-7).
     * @param typePiece Type de pièce à placer (Dame, Tour, Fou, etc.).
     * @param estBlanc  Indique si la pièce est blanche (true) ou noire (false).
     * @return Code de résultat: "OK" si réussi, "OCCUPEE" si case occupée, "INVALID" si case menacée, "ERREUR" si type invalide.
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
        this.plateauLogique[x][y] = piece; // Mise à jour du plateau logique
        return "OK";
    }

    /**
     * Retire une pièce de l'échiquier.
     * Vide la case sur l'échiquier visuel et supprime la référence dans le plateau logique.
     *
     * @param x Coordonnée X de la case (0-7).
     * @param y Coordonnée Y de la case (0-7).
     * @return true si une pièce a été retirée, false si la case était déjà vide.
     */
    public boolean retirerPiece(int x, int y) {
        Case c = echiquier.getCase(x, y);
        if (c.isEstVide()) return false;
        c.setPiece(null);
        c.setEstVide(true);
        this.plateauLogique[x][y] = null; // Mise à jour du plateau logique
        return true;
    }

    /**
     * Récupère l'objet {@link Piece} réel situé aux coordonnées données dans le plateau logique.
     * Utile pour vérifier les règles de déplacement spécifiques à chaque type de pièce.
     *
     * @param x Coordonnée X (ligne).
     * @param y Coordonnée Y (colonne).
     * @return L'objet Piece ou null si la case est vide ou hors limites.
     */
    public Piece getPieceObject(int x, int y) {
        if (x < 0 || x > 7 || y < 0 || y > 7) return null;
        return plateauLogique[x][y];
    }

    /**
     * Vérifie si le chemin entre le départ et l'arrivée est libre (sans obstacles).
     * <p>
     * Cette méthode est essentielle pour les pièces à longue portée (Tour, Fou, Dame).
     * Le Cavalier est exempté de cette règle car il peut sauter par-dessus les pièces.
     *
     * @param dx X de départ.
     * @param dy Y de départ.
     * @param ax X d'arrivée.
     * @param ay Y d'arrivée.
     * @return true si le chemin est libre, false si une pièce bloque la route.
     */
    public boolean cheminLibre(int dx, int dy, int ax, int ay) {
        Piece p = getPieceObject(dx, dy);
        if (p instanceof Cavalier) return true; // Le cavalier saute

        int deltaX = Integer.compare(ax, dx); // -1, 0, ou 1
        int deltaY = Integer.compare(ay, dy); // -1, 0, ou 1

        int currX = dx + deltaX;
        int currY = dy + deltaY;

        while (currX != ax || currY != ay) {
            if (getPieceObject(currX, currY) != null) {
                return false; // Obstacle détecté
            }
            currX += deltaX;
            currY += deltaY;
        }
        return true;
    }

    /**
     * Vérifie si la case (targetX, targetY) est attaquée par une pièce déjà présente.
     *
     * @param targetX Coordonnée X de la case cible (0-7).
     * @param targetY Coordonnée Y de la case cible (0-7).
     * @return true si la case est menacée par au moins une pièce, false sinon.
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
     * Vérifie si la configuration demandée par l'utilisateur (Mode Show) est valide.
     *
     * @param config Map contenant le nombre de pièces par type.
     * @return "OK" si la configuration est valide, sinon un message d'erreur.
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
     * Vérifie si le puzzle actuel (Mode Show) est résolu.
     *
     * @return true si le puzzle est résolu, false sinon.
     */
    public boolean estPuzzleResolu() {
        return verifierSolution(this.configurationRequise);
    }

    /**
     * Vérifie si la configuration actuelle de l'échiquier correspond à la configuration requise.
     *
     * @param configRequise Map contenant le nombre de pièces requis par type.
     * @return true si la configuration actuelle correspond, false sinon.
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
    }

    /**
     * Détermine si une pièce d'un type donné peut attaquer une case à une distance donnée.
     *
     * @param type Type de la pièce (Dame, Tour, Fou, Roi, Cavalier).
     * @param dx   Distance horizontale absolue.
     * @param dy   Distance verticale absolue.
     * @return true si la pièce peut attaquer la case, false sinon.
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
     * Retourne une représentation visuelle du plateau sous forme de tableau 2D de chaînes.
     *
     * @return Tableau 2D 8x8 contenant les symboles Unicode des pièces.
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
     *
     * @return Map contenant le nombre de pièces de chaque type présentes sur l'échiquier.
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
     *
     * @return Map contenant le nombre de pièces de chaque type.
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
     * Crée une nouvelle instance de pièce du type spécifié.
     *
     * @param type     Type de la pièce à créer (roi, dame, tour, fou, cavalier, pion).
     * @param estBlanc Indique si la pièce est blanche (true) ou noire (false).
     * @return La pièce créée, ou null si le type est invalide.
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

    /**
     * Charge une position sur l'échiquier à partir d'une chaîne FEN (Forsyth-Edwards Notation).
     * <p>
     * Cette méthode parse la chaîne FEN, vide l'échiquier actuel, et place les pièces
     * correspondantes aux positions indiquées. Elle gère également le trait (qui doit jouer).
     *
     * @param fen La chaîne de caractères FEN représentant la position.
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
                    String type = switch (Character.toLowerCase(c)) {
                        case 'k' -> "Roi";
                        case 'q' -> "Dame";
                        case 'r' -> "Tour";
                        case 'b' -> "Fou";
                        case 'n' -> "Cavalier";
                        case 'p' -> "Pion";
                        default -> null;
                    };

                    if (type != null) {
                        placerPiece(7 - i, col, type, estBlanc);
                    }
                    col++;
                }
            }
        }

        if (parties.length > 1) {
            this.traitAuBlanc = parties[1].equals("w");
        }
    }
}