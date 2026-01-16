package fr.univlorraine.pierreludmannchessmate.logic;

import fr.univlorraine.pierreludmannchessmate.model.Dame;
import fr.univlorraine.pierreludmannchessmate.model.Piece;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

/**
 * Mode de jeu « Puzzle » basé sur une séquence de coups à reproduire.
 * <p>
 * Le puzzle est chargé via un objet JSON contenant une position FEN et une
 * liste de coups au format UCI. Le joueur doit jouer exactement les coups
 * attendus, alternant avec la « réponse » de l'ordinateur. Le puzzle est
 * résolu lorsque tous les coups de la séquence ont été joués.
 */
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
    private String puzzleId = "????";

    @Getter @Setter
    private boolean scoreEnregistre = false;

    /**
     * Crée une instance du mode Puzzle et initialise les compteurs.
     */
    public JeuPuzzle() {
        super();
        this.indexCoupActuel = 0;
    }

    /**
     * Charge un puzzle depuis un objet JSON.
     * <p>
     * Clés attendues: {@code fen} (position initiale FEN), {@code moves} (coups
     * séparés par des espaces, format UCI), {@code PuzzleId} (identifiant).
     * Le premier coup de la séquence est immédiatement appliqué (coup des noirs
     * ou des blancs selon {@code traitAuBlanc}).
     *
     * @param le_pb représentation JSON du puzzle
     */
    public void dechiffre_pb(JSONObject le_pb) {
        viderPlateau();

        if (le_pb.has("fen")) {
            super.chargerFen(le_pb.getString("fen"));
        }

        if (le_pb.has("moves")) {
            this.solutionMoves = le_pb.getString("moves").split(" ");
        }

        System.out.println(le_pb);
        if (le_pb.has("PuzzleId")) {
            this.puzzleId = le_pb.getString("PuzzleId");
        } else {
            this.puzzleId = "????";
        }

        if (solutionMoves != null && solutionMoves.length > 0) {
            jouerCoupInterne(solutionMoves[0]);
            this.indexCoupActuel = 1;
        }

        this.vueJoueurEstBlanc = this.isTraitAuBlanc();
        this.partieCommencee = true;
    }

    /**
     * Tente de jouer le coup du joueur et l'évalue par rapport au coup attendu.
     *
     * @param xDep colonne de départ (0..7)
     * @param yDep ligne de départ (0..7)
     * @param xArr colonne d'arrivée (0..7)
     * @param yArr ligne d'arrivée (0..7)
     * @return "RATE" si le coup ne correspond pas, "CONTINUE" si correct mais
     *         puzzle non terminé, "GAGNE" si le puzzle est résolu, "FINI" si plus de coups
     */
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

    /**
     * Joue automatiquement la réponse de l'ordinateur (coup suivant de la
     * séquence) si nécessaire.
     */
    public void reponseOrdinateur() {
        if (this.ordiDoitJouer && solutionMoves != null && indexCoupActuel < solutionMoves.length) {
            jouerCoupInterne(solutionMoves[indexCoupActuel]);
            indexCoupActuel++;
            this.ordiDoitJouer = false;
        }
    }

    @Override
    /**
     * Indique si tous les coups de la séquence ont été joués.
     * @return {@code true} si la séquence est terminée
     */
    public boolean estPuzzleResolu() {
        if (solutionMoves == null) return false;
        return indexCoupActuel >= solutionMoves.length;
    }

    /**
     * Vérifie qu'un puzzle est chargé (au moins un coup présent).
     * @return {@code true} si la séquence de coups n'est pas vide
     */
    public boolean isPuzzleLoaded() {
        return solutionMoves != null && solutionMoves.length > 0;
    }

    /**
     * Réinitialise complètement l'état du puzzle et du plateau.
     */
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

    /**
     * Applique un coup interne au format UCI (ex: e2e4 ou e7e8q pour promo).
     * Gère la capture simple et la promotion en Dame.
     * @param uciMove coup au format UCI
     */
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

    /**
     * Fournit un indice pour le prochain coup attendu.
     * @return coordonnées "y,x" sous forme de chaîne, ou {@code null} si indisponible
     */
    public String getCoupAide() {
        if (solutionMoves == null || indexCoupActuel >= solutionMoves.length) {
            return null;
        }
        String move = solutionMoves[indexCoupActuel];
        return uciToY(move.charAt(1)) + "," + uciToX(move.charAt(0));
    }

    /**
     * Nombre de coups « joueur » (paires de coups) dans le puzzle.
     * @return un entier correspondant aux paires de coups
     */
    public int getNbCoups() {
        if (solutionMoves == null || solutionMoves.length == 0) {
            return 0;
        }
        return solutionMoves.length / 2;
    }

    private String coordsToUci(int x, int y) { return "" + (char) ('a' + x) + (char) ('1' + y); }
    private int uciToX(char c) { return c - 'a'; }
    private int uciToY(char c) { return c - '1'; }
}