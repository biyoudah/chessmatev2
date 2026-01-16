package fr.univlorraine.pierreludmannchessmate.model;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class TourTest {
    @Test
    void tourTest() {
        Tour d = new Tour(true);
        assertTrue(d.estBlanc());
    }

    @Test
    void tourTest2() {
        Tour d = new Tour(false);
        assertFalse(d.estBlanc());
    }

    @Test
    void dessinerTest() {
        Tour d= new Tour(true);
        assertEquals("\u2656",d.dessiner());
    }

    @Test
    void dessinerTest2() {
        Tour d = new Tour(false);
        assertEquals("\u265C",d.dessiner());
    }

    @Test
    void deplacementValideTest() {
        Tour d = new Tour(false);
        assertTrue(d.deplacementValide(0,0,5,0));
    }

    @Test
    void deplacementValideTest2() {
        Tour d = new Tour(false);
        assertFalse(d.deplacementValide(0,0,1,1));
    }
}
