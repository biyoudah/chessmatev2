package fr.univlorraine.pierreludmannchessmate.model;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class EchiquierTest {

    @Test
    //Echiquier vide
    void initialiserTest() {
        Echiquier e = new Echiquier();
        e.initialiser();

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                assertNotNull(e.getCase(x, y));
                assertNull(e.getCase(x, y).getPiece());
            }
        }
    }


    @Test
    //verif qu'on a une case
    void getCaseTest() {
        Echiquier e = new Echiquier();
        e.initialiser();
        Case c = e.getCase(0, 0);
        assertNotNull(c);
        assertEquals(0, c.getX());
        assertEquals(0, c.getY());
    }

    @Test
    void PlacerPieceTest() {
        Echiquier e = new Echiquier();
        e.initialiser();
        Dame d = new Dame(true);
        e.placerPiece(1,1,d);
        assertEquals("♕",e.getCase(1,1).getPiece().dessiner());
    }

    @Test
    void deplacerPieceTest() {
        Echiquier e = new Echiquier();
        e.initialiser();
        Dame d = new Dame(true);
        e.placerPiece(1,1,d);
        e.deplacerPiece(1,1,2,2);

        assertEquals("♕",e.getCase(2,2).getPiece().dessiner());
    }


    @Test
    void retirerPieceTest() {
        Echiquier e = new Echiquier();
        e.initialiser();
        Dame d = new Dame(true);
        e.placerPiece(1,1,d);
        e.retirerPiece(1,1);

        assertNull(e.getCase(2,2).getPiece());
    }

    @Test
    void getTailleTest() {
        Echiquier e = new Echiquier();
        e.initialiser();
        assertEquals(8,e.getTaille());
    }
}

