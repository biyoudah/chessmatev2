package fr.univlorraine.pierreludmannchessmate.repository;

import fr.univlorraine.pierreludmannchessmate.model.Utilisateur;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) //dit à Spring de ne pas configurer de BDD en mémoire et d'utiliser la configuration par défaut de l'application
@ActiveProfiles("test")
class UtilisateurRepositoryTest {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Test
    void should_save_and_find_utilisateur() {
        Utilisateur utilisateur = new Utilisateur("Timoute","timoute@mail.com","password");

        Utilisateur savedUser = utilisateurRepository.save(utilisateur);
        Optional<Utilisateur> foundUser = utilisateurRepository.findById(savedUser.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getPseudo()).isEqualTo("Timoute");
    }
    @Test
    void should_update_utilisateur() {
        // 1. ARRANGE : Créer et enregistrer
        Utilisateur utilisateur = new Utilisateur("AncienPseudo","mail@test.com","pass");
        utilisateurRepository.save(utilisateur);

        String nouveauPseudo = "NouveauPseudo";
        utilisateur.setPseudo(nouveauPseudo);
        utilisateurRepository.save(utilisateur);

        Optional<Utilisateur> updatedUser = utilisateurRepository.findById(utilisateur.getId());

        assertThat(updatedUser).isPresent();
        assertThat(updatedUser.get().getPseudo()).isEqualTo(nouveauPseudo);
    }
    @Test
    void should_delete_utilisateur() {
        Utilisateur utilisateur = new Utilisateur("A_Effacer","delete@mail.com","mdp");
        Utilisateur savedUser = utilisateurRepository.save(utilisateur);

        utilisateurRepository.delete(savedUser);

        Optional<Utilisateur> deletedUser = utilisateurRepository.findById(savedUser.getId());

        assertThat(deletedUser).isNotPresent();
    }
}