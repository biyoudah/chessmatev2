package fr.univlorraine.pierreludmannchessmate.model;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class RoiTest {
    @Test
    void roiTest() {
        Roi d = new Roi(true);
        assertTrue(d.estBlanc());
    }

    @Test
    void RoiTest2() {
        Roi d = new Roi(false);
        assertFalse(d.estBlanc());
    }

    @Test
    void dessinerTest() {
        Roi d= new Roi(true);
        assertEquals("\u2654",d.dessiner());
    }

    @Test
    void dessinerTest2() {
        Roi d = new Roi(false);
        assertEquals("\u265A",d.dessiner());
    }

    @Test
    void deplacementValideTest() {
        Roi d = new Roi(false);
        assertTrue(d.deplacementValide(0,0,1,0));
    }

    @Test
    void deplacementValideTest2() {
        Roi d = new Roi(false);
        assertFalse(d.deplacementValide(0,0,1,2));
    }
}
