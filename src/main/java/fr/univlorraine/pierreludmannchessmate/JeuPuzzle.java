package fr.univlorraine.pierreludmannchessmate;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

public class JeuPuzzle extends AbstractChessGame {

    private String[] solutionMoves;
    private int indexCoupActuel;

    @Getter
    private boolean partieCommencee = false;

    @Getter
    private boolean ordiDoitJouer = false;

    @Getter
    private boolean vueJoueurEstBlanc;

    // Filtre de difficulté (1, 2, 3 ou "any")
    @Getter @Setter
    private String difficulte = "any";

    @Getter @Setter
    private String puzzleId = "";

    public JeuPuzzle() {
        super();
        this.indexCoupActuel = 0;
    }

    public void dechiffre_pb(JSONObject le_pb) {
        // 1. On vide proprement le plateau avant de charger
        viderPlateau();

        // 2. Charger la position
        super.chargerFen(le_pb.getString("fen"));
        this.solutionMoves = le_pb.getString("moves").split(" ");

        if (solutionMoves.length > 0) {
            jouerCoupInterne(solutionMoves[0]);
            this.indexCoupActuel = 1;
        }

        this.vueJoueurEstBlanc = this.isTraitAuBlanc();
        this.partieCommencee = true;
    }

    public String jouerCoupJoueur(int xDep, int yDep, int xArr, int yArr) {
        String coupJoue = coordsToUci(xDep, yDep) + coordsToUci(xArr, yArr);
        if (solutionMoves == null || indexCoupActuel >= solutionMoves.length) return "FINI";

        String coupAttendu = solutionMoves[indexCoupActuel];

        if (!coupJoue.equals(coupAttendu)) {
            return "RATE";
        }

        jouerCoupInterne(coupJoue);
        indexCoupActuel++;

        if (estPuzzleResolu()) {
            this.ordiDoitJouer = false;
            return "GAGNE";
        }

        this.ordiDoitJouer = true;
        return "CONTINUE";
    }

    public void reponseOrdinateur() {
        if (this.ordiDoitJouer && solutionMoves != null && indexCoupActuel < solutionMoves.length) {
            jouerCoupInterne(solutionMoves[indexCoupActuel]);
            indexCoupActuel++;
            this.ordiDoitJouer = false;
        }
    }

    @Override
    public boolean estPuzzleResolu() {
        // PROTECTION ANTI-CRASH : Si pas de puzzle chargé, ce n'est pas résolu
        if (solutionMoves == null) return false;
        return indexCoupActuel >= solutionMoves.length;
    }

    public boolean isPuzzleLoaded() {
        return solutionMoves != null && solutionMoves.length > 0;
    }

    /**
     * Vide le plateau en utilisant les méthodes de la classe mère AbstractChessGame.
     */
    public void viderPlateau() {
        // 1. On parcourt toutes les cases pour retirer les pièces une par une
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                // retirerPiece est une méthode de AbstractChessGame
                retirerPiece(x, y);
            }
        }

        // 2. Réinitialisation des variables du puzzle
        this.solutionMoves = null;
        this.indexCoupActuel = 0;
        this.partieCommencee = false;
        this.ordiDoitJouer = false;

        // 3. On remet le trait aux Blancs par défaut pour l'affichage
        this.setTraitAuBlanc(true);

        this.puzzleId = "";
    }

    private void jouerCoupInterne(String uciMove) {
        int xDep = uciToX(uciMove.charAt(0));
        int yDep = uciToY(uciMove.charAt(1));
        int xArr = uciToX(uciMove.charAt(2));
        int yArr = uciToY(uciMove.charAt(3));

        Piece p = getPieceObject(xDep, yDep); // Méthode de la classe mère
        if (p == null) return;

        retirerPiece(xDep, yDep); // Méthode de la classe mère
        retirerPiece(xArr, yArr); // Méthode de la classe mère

        // Gestion de la promotion simple (dame par défaut)
        if (uciMove.length() > 4) {
            p = new Dame(p.estBlanc());
        }

        placerPieceInterne(xArr, yArr, p); // Méthode de la classe mère
        this.setTraitAuBlanc(!p.estBlanc());
    }

    private String coordsToUci(int x, int y) { return "" + (char) ('a' + x) + (char) ('1' + y); }
    private int uciToX(char c) { return c - 'a'; }
    private int uciToY(char c) { return c - '1'; }
}