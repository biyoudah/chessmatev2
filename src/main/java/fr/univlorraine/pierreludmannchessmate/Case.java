package fr.univlorraine.pierreludmannchessmate;

public class Case {
    private int x;
    private int y;
    private boolean estvide;
    private Piece piece;

    public Case(int x, int y, boolean estvide, Piece piece) {
        this.x = x;
        this.y = y;
        this.estvide = estvide;
    }
    public boolean isEstVide() {
        return estvide;
    }
    public void setEstVide(boolean estvide) {
        this.estvide = estvide;
    }
    public Piece getPiece() {
        return piece;
    }
    public void setPiece(Piece piece) {
        this.piece = piece;
    }
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
}
