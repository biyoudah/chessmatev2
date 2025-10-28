package fr.univlorraine.pierreludmannchessmate;


public class Fou extends Piece {
    public Fou(boolean estBlanc) {
        super(estBlanc);
    }

    public String dessiner() {
        if (estBlanc()) {
            return "\u2657";
        } else {
            return "\u265D";
        }

    }
    @Override
    public boolean deplacementValide (int departLigne, int departColonne,  int arriveeligne, int arriveColonne) {
        return (Math.abs(departLigne - arriveeligne) == Math.abs(departColonne -  arriveColonne));
    }
}
