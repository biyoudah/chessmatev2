package fr.univlorraine.pierreludmannchessmate;

import org.json.JSONObject;
import lombok.Getter;

public class JeuPuzzle extends AbstractChessGame {

    private String[] solutionMoves;
    private int indexCoupActuel;

    @Getter
    private boolean partieCommencee = false;

    // Nouvel attribut pour le pilotage du délai
    @Getter
    private boolean ordiDoitJouer = false;

    @Getter
    private boolean vueJoueurEstBlanc; // Pour fixer l'orientation

    public JeuPuzzle() {
        super();
        this.indexCoupActuel = 0;
    }

    public void dechiffre_pb(JSONObject le_pb) {
        // 1. Charger la position AVANT l'erreur de l'adversaire
        super.chargerFen(le_pb.getString("fen"));
        this.solutionMoves = le_pb.getString("moves").split(" ");

        if (solutionMoves.length > 0) {
            // 2. L'adversaire fait son erreur (ex: il joue un coup noir)
            // La méthode jouerCoupInterne va automatiquement mettre traitAuBlanc = true
            jouerCoupInterne(solutionMoves[0]);

            this.indexCoupActuel = 1; // Le coup suivant (index 1) est votre réponse
        }

        // 3. On fixe la vue du joueur sur la couleur qui doit jouer maintenant
        this.vueJoueurEstBlanc = this.isTraitAuBlanc();
        this.partieCommencee = true;
    }

    public String jouerCoupJoueur(int xDep, int yDep, int xArr, int yArr) {
        String coupJoue = coordsToUci(xDep, yDep) + coordsToUci(xArr, yArr);
        if (indexCoupActuel >= solutionMoves.length) return "FINI";

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

        // On active le drapeau pour le JavaScript
        this.ordiDoitJouer = true;
        return "CONTINUE";
    }

    public void reponseOrdinateur() {
        if (this.ordiDoitJouer && indexCoupActuel < solutionMoves.length) {
            jouerCoupInterne(solutionMoves[indexCoupActuel]);
            indexCoupActuel++;
            this.ordiDoitJouer = false;
            // Pas besoin de setTraitAuBlanc ici, jouerCoupInterne s'en occupe.
        }
    }

    @Override
    public boolean estPuzzleResolu() {
        return indexCoupActuel >= solutionMoves.length;
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

        // CHANGE LE TRAIT AUTOMATIQUEMENT
        // Si une pièce blanche a bougé, le trait passe aux noirs, et inversement.
        this.setTraitAuBlanc(!p.estBlanc());
    }

    private String coordsToUci(int x, int y) {
        return "" + (char) ('a' + x) + (char) ('1' + y);
    }

    private int uciToX(char c) { return c - 'a'; }
    private int uciToY(char c) { return c - '1'; }
}