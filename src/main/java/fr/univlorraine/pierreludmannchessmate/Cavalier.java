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
    public boolean deplacementValide(int departLigne, int departColonne, int arriveeligne, int arriveColonne) {
        // Calcul de la différence sur les Lignes (L vs L)
        int diffL = Math.abs(arriveeligne - departLigne);

        // Calcul de la différence sur les Colonnes (C vs C)
        int diffC = Math.abs(arriveColonne - departColonne);

        // Le mouvement en "L" du cavalier : 2 cases d'un côté, 1 de l'autre
        return (diffL == 2 && diffC == 1) || (diffL == 1 && diffC == 2);
    }
}


