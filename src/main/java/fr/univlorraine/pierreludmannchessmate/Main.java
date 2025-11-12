package fr.univlorraine.pierreludmannchessmate;

public class Main {
    public static void main(String[] args) {
      /*  Echiquier echiquier = new Echiquier();
        echiquier.afficher();
        Piece d = new Dame(true);
        Piece d1 = new Dame(true);
        echiquier.placerPiece(2,3,d);
        echiquier.placerPiece(3,3,d1);
        System.out.println("Echiquier apres ajout dame \n");
        echiquier.afficher();*/

                // === Création de l’échiquier ===
                Echiquier echiquier = new Echiquier();

                // === Placement de quelques pièces ===
                echiquier.placerPiece(7, 2, new Fou(true));      // Fou blanc en C1
                echiquier.placerPiece(7, 0, new Tour(true));     // Tour blanche en A1
                echiquier.placerPiece(7, 1, new Cavalier(true)); // Cavalier blanc en B1
                echiquier.placerPiece(6, 3, new Pion(true));     // Pion blanc devant la dame
                echiquier.placerPiece(0, 3, new Dame(false));    // Dame noire en D8
                echiquier.placerPiece(4, 4, new Pion(false));    // Pion noir au centre

                System.out.println("=== Plateau initial ===");
                echiquier.afficher();

                // === Tests de déplacement ===

                System.out.println("\n Faire traversé le pion par le fou spoiler ça va pas marcher");
                echiquier.deplacerPiece(7, 2, 5, 4); // diagonale -> OK
                echiquier.afficher();

                System.out.println("\n  alors que de l'autre coté");
                echiquier.deplacerPiece(7, 2, 6, 1); // diagonale -> OK
                echiquier.afficher();

                System.out.println("\n  au dessus");
                echiquier.deplacerPiece(7, 2, 5, 1); // diagonale -> OK
                echiquier.afficher();

                System.out.println("\n  faire sortir de l'echiquier");
                echiquier.deplacerPiece(7, 2, 10, 10); // diagonale -> OK
                echiquier.afficher();




        }


    }

