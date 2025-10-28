package fr.univlorraine.pierreludmannchessmate;

public class Roi extends Piece {

    public Roi(boolean estBlanc) {
        super(estBlanc);
    }

    @Override
    public String dessiner() {
        if (estBlanc()) {
            return "\u2654"; // ♔
        } else {
            return "\u265A"; // ♚
        }
    }

    @Override
    public boolean deplacementValide(int departLigne, int departColonne,  int arriveeligne, int arriveColonne) {
        int diffL = Math.abs(arriveeligne - departLigne);
        int diffC = Math.abs(arriveColonne - departColonne);
        // Le roi se déplace d'une case dans n'importe quelle direction
        return (diffL <= 1 && diffC <= 1) && !(diffL == 0 && diffC == 0);
    }
}

