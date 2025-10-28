package fr.univlorraine.pierreludmannchessmate;

public class Main {
    public static void main(String[] args) {
        Echiquier echiquier = new Echiquier();
        echiquier.afficher();
        Piece d = new Dame(true);
        echiquier.placerPiece(2,3,d);
        System.out.println("Echiquier apres ajout dame \n");
        echiquier.afficher();

    }
}
