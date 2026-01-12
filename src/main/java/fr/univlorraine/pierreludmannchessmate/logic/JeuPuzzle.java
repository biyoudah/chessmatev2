package fr.univlorraine.pierreludmannchessmate.logic;

import fr.univlorraine.pierreludmannchessmate.model.Dame;
import fr.univlorraine.pierreludmannchessmate.model.Piece;
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

    @Getter @Setter
    private String difficulte = "any";

    @Getter @Setter
    private String puzzleId = "????"; // Valeur par défaut pour l'affichage

    @Getter @Setter
    private boolean scoreEnregistre = false;

    public JeuPuzzle() {
        super();
        this.indexCoupActuel = 0;
    }

    public void dechiffre_pb(JSONObject le_pb) {
        // 1. On vide proprement le plateau avant de charger
        viderPlateau();

        // 2. Charger la position
        if (le_pb.has("fen")) {
            super.chargerFen(le_pb.getString("fen"));
        }

        if (le_pb.has("moves")) {
            this.solutionMoves = le_pb.getString("moves").split(" ");
        }

        // --- RECUPERATION SECURISEE DE L'ID ---
        // Vérifie les différentes orthographes possibles dans le CSV
        System.out.println(le_pb);
        if (le_pb.has("PuzzleId")) {
            this.puzzleId = le_pb.getString("PuzzleId");
        } else {
            this.puzzleId = "????";
        }

        // Jouer le premier coup de l'ordinateur (mise en place du puzzle)
        if (solutionMoves != null && solutionMoves.length > 0) {
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
        if (solutionMoves == null) return false;
        return indexCoupActuel >= solutionMoves.length;
    }

    public boolean isPuzzleLoaded() {
        return solutionMoves != null && solutionMoves.length > 0;
    }

    public void viderPlateau() {
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                retirerPiece(x, y);
            }
        }
        this.solutionMoves = null;
        this.indexCoupActuel = 0;
        this.partieCommencee = false;
        this.ordiDoitJouer = false;
        this.setTraitAuBlanc(true);
        this.puzzleId = "????";
    }

    private void jouerCoupInterne(String uciMove) {
        int xDep = uciToX(uciMove.charAt(0));
        int yDep = uciToY(uciMove.charAt(1));
        int xArr = uciToX(uciMove.charAt(2));
        int yArr = uciToY(uciMove.charAt(3));

        Piece p = getPieceObject(xDep, yDep);
        if (p == null) return;

        retirerPiece(xDep, yDep);
        retirerPiece(xArr, yArr);

        if (uciMove.length() > 4) {
            p = new Dame(p.estBlanc());
        }

        placerPieceInterne(xArr, yArr, p);
        this.setTraitAuBlanc(!p.estBlanc());
    }

    public String getCoupAide() {
        if (solutionMoves == null || indexCoupActuel >= solutionMoves.length) {
            return null;
        }
        String move = solutionMoves[indexCoupActuel];
        return uciToY(move.charAt(1)) + "," + uciToX(move.charAt(0));
    }

    public int getNbCoups() {
        if (solutionMoves == null || solutionMoves.length == 0) {
            return 0;
        }
        // Division entière par 2 donne le nombre de paires de coups
        return solutionMoves.length / 2;
    }

    private String coordsToUci(int x, int y) { return "" + (char) ('a' + x) + (char) ('1' + y); }
    private int uciToX(char c) { return c - 'a'; }
    private int uciToY(char c) { return c - '1'; }
}