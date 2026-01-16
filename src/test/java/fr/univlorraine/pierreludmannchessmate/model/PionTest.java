package fr.univlorraine.pierreludmannchessmate.model;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class PionTest {
    @Test
    void constructeurPionTest() {
        Pion d = new Pion(true);
        assertTrue(d.estBlanc());
    }

    @Test
    void constructeurPionTest2() {
        Pion d = new Pion(false);
        assertFalse(d.estBlanc());
    }

    @Test
    void dessinerTest() {
        Pion d= new Pion(true);
        assertEquals("\u2659",d.dessiner());
    }

    @Test
    void dessinerTest2() {
        Pion d = new Pion(false);
        assertEquals("\u265F",d.dessiner());
    }
    

    @Test
    void deplacementValideTest() {
        Pion d = new Pion(false);
        assertTrue(d.deplacementValide(0,0,1,0));
    }

    @Test
    void deplacementValideTest2() {
        Pion d = new Pion(false);
        assertFalse(d.deplacementValide(0,0,1,2));
    }
}
