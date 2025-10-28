package fr.univlorraine.pierreludmannchessmate;

public class Cavalier extends Piece {

    public Cavalier(boolean estBlanc) {
        super(estBlanc);
    }

    @Override
    public String dessiner() {
        if (estBlanc()) {
            return "\u2658"; // ♘
        } else {
            return "\u265E"; // ♞
        }
    }

    @Override
    // Soit diff de 1 en ligne et 2 en colonne ou l'inverse
    public boolean deplacementValide(int departLigne, int departColonne,  int arriveeligne, int arriveColonne) {
        int diffL = Math.abs(arriveeligne - departColonne);
        int diffC = Math.abs(arriveColonne - departColonne);

        return (diffL == 2 && diffC == 1) || (diffL == 1 && diffC == 2);
    }
}


