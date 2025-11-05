package fr.univlorraine.pierreludmannchessmate;

public class Main {
    public static void main(String[] args) {
      /*  Echiquier echiquier = new Echiquier();
        echiquier.afficher();
        Piece d = new Dame(true);
        echiquier.placerPiece(2,3,d);
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

                System.out.println("\nTest 1 : Déplacement valide du Fou (C1 -> E3)");
                echiquier.deplacerPiece(7, 2, 5, 4); // diagonale -> OK
                echiquier.afficher();

                System.out.println("\nTest 2 : Déplacement invalide du Fou (E3 -> E5)");
                echiquier.deplacerPiece(5, 4, 3, 4); // vertical -> NON
                echiquier.afficher();

                System.out.println("\nTest 3 : Tentative de déplacement bloqué (Tour A1 -> A4)");
                echiquier.placerPiece(6, 0, new Pion(true)); // Pion devant la tour
                echiquier.deplacerPiece(7, 0, 4, 0); // Bloqué -> NON
                echiquier.afficher();

                System.out.println("\nTest 4 : Déplacement du Cavalier (B1 -> C3)");
                echiquier.deplacerPiece(7, 1, 5, 2); // OK
                echiquier.afficher();

                System.out.println("\nTest 5 : Capture de la dame noire (E3 -> D8)");
                echiquier.deplacerPiece(5, 4, 0, 3); // fou attaque la dame -> OK
                echiquier.afficher();

        }


    }

