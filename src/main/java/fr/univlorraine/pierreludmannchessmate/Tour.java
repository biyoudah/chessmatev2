package fr.univlorraine.pierreludmannchessmate;

public class Tour extends Piece {

    public Tour(boolean estBlanc) {
        super(estBlanc);
    }

    @Override
    public String dessiner() {
        if (estBlanc()) {
            return "\u2656"; // ♖
        } else {
            return "\u265C"; // ♜
        }
    }

    @Override
    public boolean deplacementValide(int departLigne, int departColonne,  int arriveeligne, int arriveColonne) {
        // La tour se déplace horizontalement ou verticalement
        return (departLigne == arriveeligne || departColonne == arriveColonne);
    }
}


