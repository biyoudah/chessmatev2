package fr.univlorraine.pierreludmannchessmate;

public class Tour extends Piece {

    public Tour(boolean estBlanc) {
        super(estBlanc);
    }

    @Override
    public void dessiner() {
        if (estBlanc()) {
            System.out.println("\u2656"); // ♖
        } else {
            System.out.println("\u265C"); // ♜
        }
    }

    @Override
    public boolean deplacementValide(int departLigne, int departColonne,  int arriveeligne, int arriveColonne) {
        // La tour se déplace horizontalement ou verticalement
        return (departLigne == arriveeligne || departColonne == arriveColonne);
    }
}


