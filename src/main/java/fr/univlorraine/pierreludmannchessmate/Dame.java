package fr.univlorraine.pierreludmannchessmate;

public class Dame extends Piece {

    public Dame(boolean estBlanc) {
        super(estBlanc);
    }

    @Override
    public void dessiner() {
        if (estBlanc()) {
            System.out.println("\u2655"); // ♕
        } else {
            System.out.println("\u265B"); // ♛
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

