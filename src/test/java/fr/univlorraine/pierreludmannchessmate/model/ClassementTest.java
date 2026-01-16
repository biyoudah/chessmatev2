package fr.univlorraine.pierreludmannchessmate.model;

import org.junit.jupiter.api.Test;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ClassementTest {

    @Test
    void constructeur_Default_InitializeClassement() {
        Classement classement = new Classement();

        assertNotNull(classement);
        assertNull(classement.getNom());
        assertNull(classement.getDateMAJ());
    }

    @Test
    void constructeur_AvecNom_InitializeNomAndDateMAJ() {
        Instant before = Instant.now();
        Classement classement = new Classement("Champion 2024");
        Instant after = Instant.now();

        assertEquals("Champion 2024", classement.getNom());
        assertNotNull(classement.getDateMAJ());
        assertTrue(classement.getDateMAJ().isAfter(before.minusSeconds(1)));
        assertTrue(classement.getDateMAJ().isBefore(after.plusSeconds(1)));
    }

    @Test
    void setNom_UpdatesNomCorrectly() {
        Classement classement = new Classement();
        classement.setNom("Grand Tournoi");

        assertEquals("Grand Tournoi", classement.getNom());
    }

    @Test
    void setDateMAJ_UpdatesDateMAJCorrectly() {
        Classement classement = new Classement();
        Instant now = Instant.now();
        classement.setDateMAJ(now);

        assertEquals(now, classement.getDateMAJ());
    }

    @Test
    void getNom_ReturnsSetNom() {
        Classement classement = new Classement("Manche 1");

        assertEquals("Manche 1", classement.getNom());
    }

    @Test
    void getDateMAJ_ReturnsSetDateMAJ() {
        Classement classement = new Classement();
        Instant testDate = Instant.now();
        classement.setDateMAJ(testDate);

        assertEquals(testDate, classement.getDateMAJ());
    }

    @Test
    void deux_ClassementAvecMemNom_SontDifferents() {
        Classement c1 = new Classement("Test");
        Classement c2 = new Classement("Test");
        assertNotEquals(c1, c2);
    }

    @Test
    void classement_AvecNom_EstValide() {
        Classement classement = new Classement("Classement Valide");

        assertNotNull(classement.getNom());
        assertTrue(classement.getNom().length() > 0);
        assertNotNull(classement.getDateMAJ());
    }
}
