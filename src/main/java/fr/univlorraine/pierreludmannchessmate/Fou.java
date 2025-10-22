package fr.univlorraine.pierreludmannchessmate;


public class Fou extends Piece {
    public Fou(boolean estBlanc) {
        super(estBlanc);
    }

    public void dessiner() {
        if (estBlanc()) {
            System.out.println("\u2657");
        } else {
            System.out.println("\u265D");
        }

    }
    @Override
    public boolean deplacementValide (int departLigne, int departColonne,  int arriveeligne, int arriveColonne) {
        return (Math.abs(departLigne - arriveeligne) == Math.abs(departColonne -  arriveColonne));
    }
}
