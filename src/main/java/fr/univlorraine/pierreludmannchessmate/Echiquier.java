package fr.univlorraine.pierreludmannchessmate;

public class Echiquier {
    private final Case[][] e;

    public Echiquier(){
        e = new Case[8][8];
        initialiser();
    }

    public void initialiser(){
        for (int i = 0; i < 8; i++){
            for (int j = 0; j < 8; j++){
                e[i][j] = new Case(i,j,true,null);
            }
        }
    }

    public Case getCase(int x, int y){
        return e[x][y];
    }

    private boolean coordonneesValides(int x, int y){
        return x >= 0 && x < 8 && y >= 0 && y < 8;
    }

    private boolean cheminlibre(int x1, int y1, int x2, int y2, Piece piece){

        int deltaX = Integer.compare(x2, x1); // plus petit = -1, 0,plus grand = 1
        int deltaY = Integer.compare(y2, y1); // -1, 0, 1

        int i = x1 + deltaX;
        int j = y1 + deltaY;

        while (i != x2 || j != y2) {
            if (e[i][j].getPiece() != null) {
                return false; // une pièce bloque le chemin
            }
            i += deltaX;
            j += deltaY;
        }

        return true;
    }

    public boolean deplacerPiece(int x1, int y1, int x2, int y2) {
        Piece piece = e[x1][y1].getPiece();

        if (!coordonneesValides(x1, y1) || !coordonneesValides(x2, y2)) {
            // coordonées invalide
            return false;
        }

        if (piece == null) return false;

        Case caseDepart = e[x1][y1];
        Case caseArrivee = e[x2][y2];

        if (!piece.deplacementValide(x1, y1, x2, y2)) {
            System.out.println("Déplacement non valide pour cette pièce !");
            return false;
        }
        if (!cheminlibre(x1, y1, x2, y2, piece)) {
            System.out.println("Le chemin est bloqué !");
            return false;
        }
        caseArrivee.setPiece(piece);
        caseDepart.setPiece(null);
        caseDepart.setEstVide(true);
        caseArrivee.setEstVide(false);

        System.out.println("Déplacement effectué !");
        return true;
    }


    public void placerPiece(int x1, int y1, Piece piece) {
        if(e[x1][y1].isEstVide()){
            e[x1][y1].setPiece(piece);
            e[x1][y1].setEstVide(false);
        }
    }


    public void afficher() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece p = e[i][j].getPiece();
                System.out.print((p == null ? "." : p.dessiner()) + " ");
            }
            System.out.println();
        }


    }
    public int getTaille() {
        return e.length;
    }
}
