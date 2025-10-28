package fr.univlorraine.pierreludmannchessmate;

public class Dame extends Piece {

    public Dame(boolean estBlanc) {
        super(estBlanc);
    }

    @Override
    public String dessiner() {
        if (estBlanc()) {
            return "\u2655"; // ♕
        } else {
            return "\u265B"; // ♛
        }
    }

    @Override
    public boolean deplacementValide(int departLigne, int departColonne,  int arriveeligne, int arriveColonne) {
        // La dame combine les mouvements du fou et de la tour
        boolean diagonale = Math.abs(arriveeligne - departLigne) == Math.abs(arriveColonne - departColonne);
        boolean ligneOuColonne = (departLigne == arriveeligne || departColonne == arriveColonne);
        return diagonale || ligneOuColonne;
    }
}

