package fr.univlorraine.pierreludmannchessmate;


public abstract class Piece{
    private boolean estBlanc; // Vrai blanc, noir faux

    public Piece(boolean estBlanc){
        this.estBlanc = estBlanc;
    }
    public boolean estBlanc(){ return estBlanc; }
    public abstract boolean deplacementValide (int departLigne, int departColonne,  int arriveeligne, int arriveColonne);
    public abstract void dessiner();

}