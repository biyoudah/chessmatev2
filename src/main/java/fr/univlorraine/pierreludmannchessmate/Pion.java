package fr.univlorraine.pierreludmannchessmate;

public class Pion extends Piece {

    public Pion(boolean estBlanc) {
        super(estBlanc);
    }

    @Override
    public void dessiner() {
        if (estBlanc()) {
            System.out.println("\u2659"); // ♙
        } else {
            System.out.println("\u265F"); // ♟
        }
    }

    @Override
    public boolean deplacementValide(int departLigne, int departColonne,  int arriveeligne, int arriveColonne) {
        int diffL = arriveeligne - departLigne;
        int diffC = Math.abs(arriveColonne - departColonne);

        if (estBlanc()) {
            // Pion blanc monte (lignes décroissantes)
            return (diffL == -1 && diffC == 0); // avance d'une case tout droit
        } else {
            // Pion noir descend (lignes croissantes)
            return (diffL == 1 && diffC == 0);
        }
    }
}


