package fr.univlorraine.pierreludmannchessmate;
import static org.junit.jupiter.api.Assertions.*;

import fr.univlorraine.pierreludmannchessmate.model.Dame;
import org.junit.jupiter.api.Test;

class DameTest {
    @Test
    void DameTest() {
        Dame d = new Dame(true);
        assertTrue(d.estBlanc());
    }

    @Test
    void DameTest2() {
        Dame d = new Dame(false);
        assertFalse(d.estBlanc());
    }

    @Test
    void dessinerTest() {
        Dame d= new Dame(true);
        assertEquals("\u2655",d.dessiner());
    }

    @Test
    void dessinerTest2() {
        Dame d = new Dame(false);
        assertEquals("\u265B",d.dessiner());
    }

    @Test
    void deplacementValideTest() {
        Dame d = new Dame(false);
        assertTrue(d.deplacementValide(0,0,1,1));
    }

    @Test
    void deplacementValideTest2() {
        Dame d = new Dame(false);
        assertFalse(d.deplacementValide(0,0,1,2));
    }
}
