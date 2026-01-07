package fr.univlorraine.pierreludmannchessmate;
import static org.junit.jupiter.api.Assertions.*;

import fr.univlorraine.pierreludmannchessmate.model.Fou;
import org.junit.jupiter.api.Test;

class FouTest {
    @Test
    void FouTest() {
        Fou d = new Fou(true);
        assertTrue(d.estBlanc());
    }

    @Test
    void FouTest2() {
        Fou d = new Fou(false);
        assertFalse(d.estBlanc());
    }

    @Test
    void dessinerTest() {
        Fou d= new Fou(true);
        assertEquals("\u2657",d.dessiner());
    }

    @Test
    void dessinerTest2() {
        Fou d = new Fou(false);
        assertEquals("\u265D",d.dessiner());
    }

    @Test
    void deplacementValideTest() {
        Fou d = new Fou(false);
        assertTrue(d.deplacementValide(0,0,1,1));
    }

    @Test
    void deplacementValideTest2() {
        Fou d = new Fou(false);
        assertFalse(d.deplacementValide(0,0,1,2));
    }
}
