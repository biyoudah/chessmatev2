package fr.univlorraine.pierreludmannchessmate;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class JoueurTest {

    @Test
    public void JoueurTest() {
        Joueur j  = new Joueur("Joueur",true);
        assertEquals("Joueur",j.getPseudo());
        assertTrue(j.estBlanc());
        assertFalse(j.isEnEchec());
    }
}
