package fr.univlorraine.pierreludmannchessmate.model;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CavalierTest {
    @Test
    void CavalierTest() {
        Cavalier cavalier = new Cavalier(true);
        assertTrue(cavalier.estBlanc());
    }

    @Test
    void CavalierTest2() {
        Cavalier cavalier = new Cavalier(false);
        assertFalse(cavalier.estBlanc());
    }

    @Test
    void dessinerTest() {
        Cavalier cavalier = new Cavalier(true);
        assertEquals("\u2658",cavalier.dessiner());
    }

    @Test
    void dessinerTest2() {
        Cavalier cavalier = new Cavalier(false);
        assertEquals("\u265E",cavalier.dessiner());
    }

    @Test
    void deplacementValideTest() {
        Cavalier cavalier = new Cavalier(false);
        assertTrue(cavalier.deplacementValide(0,0,1,2));
    }

    @Test
    void deplacementValideTest2() {
        Cavalier cavalier = new Cavalier(false);
        assertFalse(cavalier.deplacementValide(0,0,1,1));
    }
}
