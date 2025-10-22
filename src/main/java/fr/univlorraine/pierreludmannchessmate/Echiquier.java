package fr.univlorraine.pierreludmannchessmate;

public class Echiquier {
    private Piece [][] cases;

    public Echiquier(){
        this.cases = new Piece[8][8];
        //initialiser();
    }

    private void initialiser(){

    }

    public Piece getPiece(int x, int y) {
        return cases[x][y];
    }

    public boolean estVide(int x, int y) {
        return cases[x][y] == null;
    }

    public boolean deplacerPiece(int x1, int y1, int x2, int y2) {
        Piece piece = getPiece(x1, y1);
        if (piece == null) return false;

        if (piece.deplacementValide(x2, y2, this)) {
            cases[x2][y2] = piece;
            cases[x1][y1] = null;
            piece.setPosition(x2, y2);
            return true;
        }
        return false;
    }

    public void placerPiece(Piece piece) {
        if(estVide(piece.x, piece.y)){
            cases[piece.x][piece.y] = piece;
        }
    }

    public void afficher() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece p = cases[i][j];
                System.out.print((p == null ? "." : p.symbole()) + " ");
            }
            System.out.println();
        }
    }
}
