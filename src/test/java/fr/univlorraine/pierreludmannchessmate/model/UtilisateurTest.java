package fr.univlorraine.pierreludmannchessmate.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UtilisateurTest {

    @Test
    void constructeurParDefaut_InitialiseUtilisateur() {
        Utilisateur utilisateur = new Utilisateur();

        assertNotNull(utilisateur);
        assertNull(utilisateur.getPseudo());
        assertNull(utilisateur.getEmail());
        assertNull(utilisateur.getPassword());
    }

    @Test
    void constructeurAvecParametres_InitialiseCorrectement() {
        Utilisateur utilisateur = new Utilisateur("Alice", "alice@test.com", "password123");

        assertEquals("Alice", utilisateur.getPseudo());
        assertEquals("alice@test.com", utilisateur.getEmail());
        assertEquals("password123", utilisateur.getPassword());
    }

    @Test
    void definirPseudo_ModifiePseudoCorrectement() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setPseudo("Bob");

        assertEquals("Bob", utilisateur.getPseudo());
    }

    @Test
    void definirEmail_ModifieEmailCorrectement() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail("bob@test.com");

        assertEquals("bob@test.com", utilisateur.getEmail());
    }

    @Test
    void definirMotDePasse_ModifieMotDePasseCorrectement() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setPassword("securePassword");

        assertEquals("securePassword", utilisateur.getPassword());
    }

    @Test
    void definirRole_ModifieRoleCorrectement() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setRole("ADMIN");

        assertEquals("ADMIN", utilisateur.getRole());
    }

    @Test
    void definirTempsTotalDeJeu_ModifieTempsTotalCorrectement() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setTempsTotalDeJeu(3600);  // 1 heure en secondes

        assertEquals(3600, utilisateur.getTempsTotalDeJeu());
    }

    @Test
    void obtenirPseudo_RetournePseudoDefinit() {
        Utilisateur utilisateur = new Utilisateur("Charlie", "charlie@test.com", "pass123");

        assertEquals("Charlie", utilisateur.getPseudo());
    }

    @Test
    void obtenirEmail_RetourneEmailDefinit() {
        Utilisateur utilisateur = new Utilisateur("Dave", "dave@test.com", "pass456");

        assertEquals("dave@test.com", utilisateur.getEmail());
    }

    @Test
    void obtenirMotDePasse_RetourneMotDePasseDefinit() {
        Utilisateur utilisateur = new Utilisateur("Eve", "eve@test.com", "pass789");

        assertEquals("pass789", utilisateur.getPassword());
    }

    @Test
    void tempsTotalDeJeuParDefaut_EstZero() {
        Utilisateur utilisateur = new Utilisateur();

        assertEquals(0, utilisateur.getTempsTotalDeJeu());
    }

    @Test
    void roleParDefaut_EstNull() {
        Utilisateur utilisateur = new Utilisateur();

        assertNull(utilisateur.getRole());
    }

    @Test
    void identifiant_EstAutoGenere() {
        Utilisateur utilisateur = new Utilisateur("Henry", "henry@test.com", "pass222");

        // L'ID est null avant la persistance en base (pas d'ID en mémoire)
        assertNull(utilisateur.getId());
    }

    @Test
    void utilisateur_AvecTousParametres_EstValide() {
        Utilisateur utilisateur = new Utilisateur("Iris", "iris@test.com", "pass333");
        utilisateur.setRole("USER");
        utilisateur.setTempsTotalDeJeu(7200);

        assertEquals("Iris", utilisateur.getPseudo());
        assertEquals("iris@test.com", utilisateur.getEmail());
        assertEquals("pass333", utilisateur.getPassword());
        assertEquals("USER", utilisateur.getRole());
        assertEquals(7200, utilisateur.getTempsTotalDeJeu());
    }
}
