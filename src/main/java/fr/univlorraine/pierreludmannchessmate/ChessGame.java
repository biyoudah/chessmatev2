package fr.univlorraine.pierreludmannchessmate;


import lombok.Getter;
import lombok.Setter;

public class ChessGame {

    private Echiquier echiquier;

    @Getter
    private Joueur joueur;

    @Getter @Setter
    private int score;

    @Getter @Setter
    private String modeDeJeu; // "puzzle-api", "custom", etc.

    @Getter @Setter
    private String solutionPuzzle; // Stocke le PGN (la solution)

    @Getter @Setter
    private boolean traitAuBlanc; // true = aux blancs de jouer, false = noirs

    // --- CONSTRUCTEURS ---

    public ChessGame() {
        this.echiquier = new Echiquier();
        this.joueur = new Joueur("Joueur", true);
        this.score = 0;
        this.modeDeJeu = "custom";
        this.traitAuBlanc = true;
    }

    public ChessGame(String pseudo) {
        this.echiquier = new Echiquier();
        this.joueur = new Joueur(pseudo, true);
        this.score = 0;
        this.modeDeJeu = "custom";
        this.traitAuBlanc = true;
    }

    // =========================================================================
    // SECTION 1 : LOGIQUE DE MOUVEMENT (POUR LE PUZZLE)
    // =========================================================================

    /**
     * Tente de déplacer une pièce existante du point A (depart) au point B (arrivee).
     * Utilisé dans le mode Puzzle.
     * @return un code statut : "OK", "VIDE", "INVALID", "BLOCAGE" ou "AMI".
     */
    public String deplacerPiece(int xDepart, int yDepart, int xArrivee, int yArrivee) {

        Case caseDepart = echiquier.getCase(xDepart, yDepart);
        Case caseArrivee = echiquier.getCase(xArrivee, yArrivee);

        // 1. Vérifier s'il y a bien une pièce à déplacer
        if (caseDepart.isEstVide() || caseDepart.getPiece() == null) {
            return "VIDE";
        }

        Piece piece = caseDepart.getPiece();

        // (Optionnel) Vérifier si c'est bien au tour de cette couleur de jouer
        // if (piece.isEstBlanc() != this.traitAuBlanc) return "MAUVAIS_TOUR";

        // 2. Vérifier la géométrie du coup (Est-ce que cette pièce bouge comme ça ?)
        if (!piece.deplacementValide(xDepart, yDepart, xArrivee, yArrivee)) {
            return "INVALID";
        }

        // 3. Vérifier si le chemin est bloqué (Sauf pour le Cavalier qui saute)
        if (!piece.getClass().getSimpleName().equalsIgnoreCase("Cavalier")) {
            if (!cheminEstLibre(xDepart, yDepart, xArrivee, yArrivee)) {
                return "BLOCAGE"; // Il y a une pièce sur le chemin
            }
        }

        // 4. Vérifier la case d'arrivée (Tir allié ?)
        if (!caseArrivee.isEstVide()) {
            Piece pieceCible = caseArrivee.getPiece();
            if (pieceCible.estBlanc() == piece.estBlanc()) {
                return "AMI"; // Impossible de manger sa propre pièce
            }
            // Sinon, c'est une capture, la pièce cible disparaitra (écrasée)
        }

        // --- EXÉCUTION DU MOUVEMENT ---
        echiquier.placerPiece(xArrivee, yArrivee, piece); // Place la pièce à l'arrivée
        caseDepart.setPiece(null);       // Vide le départ
        caseDepart.setEstVide(true);

        // Change le trait
        this.traitAuBlanc = !this.traitAuBlanc;

        return "OK";
    }

    /**
     * Vérifie qu'il n'y a pas d'obstacle entre (x1,y1) et (x2,y2).
     * Fonctionne pour les lignes droites et les diagonales.
     */
    private boolean cheminEstLibre(int x1, int y1, int x2, int y2) {
        int dx = Integer.compare(x2, x1); // -1, 0, ou 1
        int dy = Integer.compare(y2, y1); // -1, 0, ou 1

        // On part de la première case après le départ
        int x = x1 + dx;
        int y = y1 + dy;

        // Tant qu'on n'a pas atteint la case d'arrivée
        while (x != x2 || y != y2) {
            if (!echiquier.getCase(x, y).isEstVide()) {
                return false; // Obstacle trouvé
            }
            x += dx;
            y += dy;
        }
        return true;
    }

    // =========================================================================
    // SECTION 2 : LOGIQUE API CHESS.COM (FEN)
    // =========================================================================

    /**
     * Initialise le plateau à partir d'une chaîne FEN.
     * Ex: "r1bqkbnr/pppp1ppp/2n5/4p3... w KQkq - 0 1"
     */
    public void chargerDepuisFen(String fen) {
        this.reinitialiser();

        String[] parties = fen.split(" ");
        String position = parties[0];

        // Gestion du trait (qui doit jouer ?)
        if (parties.length > 1) {
            this.traitAuBlanc = parties[1].equals("w");
        }

        String[] rangees = position.split("/");

        // FEN décrit le plateau du rang 8 (haut) au rang 1 (bas)
        // L'index 0 correspond à la ligne du haut (x=0 dans notre logique visuelle standard)
        for (int row = 0; row < 8; row++) {
            String ligne = rangees[row];
            int col = 0;

            for (char c : ligne.toCharArray()) {
                if (Character.isDigit(c)) {
                    // C'est un nombre de cases vides
                    col += Character.getNumericValue(c);
                } else {
                    // C'est une pièce
                    boolean estBlanc = Character.isUpperCase(c);
                    String type = getTypeFromFenChar(c);

                    Piece p = creerPiece(type, estBlanc);
                    if (p != null) {
                        // Attention : row correspond souvent à l'axe vertical (X ou Y selon ton implémentation)
                        // Ici je suppose que echiquier.placerPiece prend (Ligne, Colonne)
                        echiquier.placerPiece(row, col, p);
                    }
                    col++;
                }
            }
        }
    }

    private String getTypeFromFenChar(char c) {
        return switch (Character.toLowerCase(c)) {
            case 'p' -> "pion";
            case 'r' -> "tour";
            case 'n' -> "cavalier";
            case 'b' -> "fou";
            case 'q' -> "dame";
            case 'k' -> "roi";
            default -> "pion";
        };
    }

    // =========================================================================
    // SECTION 3 : LOGIQUE PLACEMENT LIBRE (MODE 8-REINES / CUSTOM)
    // =========================================================================

    /**
     * Crée et place une nouvelle pièce (Mode Éditeur / 8 Reines).
     */
    public String placerPiece(int x, int y, String typePiece, boolean estBlanc) {
        Case caseDestination = echiquier.getCase(x, y);

        if (!caseDestination.isEstVide()) return "OCCUPEE";
        if (estEnConflit(x, y)) return "INVALID"; // Vérifie si la case est attaquée

        // Note: estMenacant vérifie si la nouvelle pièce attaque les autres
        // if(estMenacant(x,y,typePiece,estBlanc)) return "MENACANT";

        Piece piece = creerPiece(typePiece, estBlanc);
        if (piece == null) return "ERREUR";

        echiquier.placerPiece(x, y, piece);
        return "OK";
    }

    public boolean retirerPiece(int x, int y) {
        Case caseSource = echiquier.getCase(x, y);
        if (caseSource.isEstVide()) return false;

        caseSource.setPiece(null);
        caseSource.setEstVide(true);
        return true;
    }

    /**
     * Vérifie si la case (x, y) est menacée par une pièce existante.
     */
    private boolean estEnConflit(int x, int y) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Case c = echiquier.getCase(i, j);
                if (!c.isEstVide() && c.getPiece() != null) {
                    // On vérifie le déplacement mais on ignore les collisions pour simplifier
                    // la logique "8 reines" pure si besoin, sinon on garde deplacementValide
                    if (c.getPiece().deplacementValide(i, j, x, y)) {
                        // Pour être précis dans le mode 8 reines, il faudrait aussi vérifier le chemin
                        // Mais pour l'instant on garde ça simple
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean estPuzzleResolu() {
        if ("custom".equals(modeDeJeu)) {
            return verifier8Reines();
        }
        return false; // TODO: Logique de résolution API (comparer avec PGN)
    }

    private boolean verifier8Reines() {
        int countDames = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (!echiquier.getCase(i, j).isEstVide()) countDames++;
            }
        }
        return countDames == 8;
    }

    // =========================================================================
    // UTILITAIRES
    // =========================================================================

    public void reinitialiser() {
        echiquier.initialiser(); // Remet tout à zéro (vide)
        score = 0;
        traitAuBlanc = true;
    }

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
        // Adapte les constructeurs selon tes classes réelles
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