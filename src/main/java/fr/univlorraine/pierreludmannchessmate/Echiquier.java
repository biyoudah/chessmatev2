package fr.univlorraine.pierreludmannchessmate;

public class Echiquier {
    private Case[][] e;

    public Echiquier(){
        this.e = new Case[8][8];
    }
    public void initialiser(Echiquier e){
        e = new Echiquier();
    }

    public Case getCase(int x, int y){
        return e[x][y];
    }
    public boolean deplacerPiece(int x1, int y1, int x2, int y2) {
        Piece piece = e[x1][y1].getPiece();
        if (piece == null) return false;

        if (piece.deplacementValide(x1, y1, x2, y2)){
            e[x2][y2].setPiece(piece);
            e[x1][y1] = null;
            return true;
        }
        return false;
    }
    public void placerPiece(int x1, int y1, Piece piece) {
        if(e[x1][y1].isEstVide()){
            e[x1][y1].setPiece(piece);
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
}
