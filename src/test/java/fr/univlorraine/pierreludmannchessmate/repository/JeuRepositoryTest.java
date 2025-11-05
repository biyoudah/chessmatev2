package fr.univlorraine.pierreludmannchessmate.repository;

import java.util.Optional;
import fr.univlorraine.pierreludmannchessmate.model.Jeu;
import fr.univlorraine.pierreludmannchessmate.model.Difficulte;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class JeuRepositoryTest {

    @Autowired
    private JeuRepository jeuRepository;

    @Test
    void should_save_and_find_jeu() {
        // ARRANGE
        Jeu jeu = new Jeu("Partie d'échecs 1", "{}", Difficulte.facile);

        // ACT
        Jeu savedJeu = jeuRepository.save(jeu);
        Optional<Jeu> foundJeu = jeuRepository.findById(savedJeu.getId());

        // ASSERT
        assertThat(foundJeu).isPresent();
        assertThat(foundJeu.get().getNom()).isEqualTo("Partie d'échecs 1");
        assertThat(foundJeu.get().getDifficulte()).isEqualTo(Difficulte.facile);
    }

    @Test
    void should_update_jeu() {
        // ARRANGE
        Jeu jeu = new Jeu("Partie Initiale", "{}", Difficulte.normal);
        jeuRepository.save(jeu);

        // ACT
        String nouveauNom = "Partie Modifiée";
        jeu.setNom(nouveauNom);
        jeuRepository.save(jeu);

        // ASSERT
        Optional<Jeu> updatedJeu = jeuRepository.findById(jeu.getId());
        assertThat(updatedJeu).isPresent();
        assertThat(updatedJeu.get().getNom()).isEqualTo(nouveauNom);
    }

    @Test
    void should_delete_jeu() {
        // ARRANGE
        Jeu jeu = new Jeu("Jeu à supprimer", "{}", Difficulte.facile);
        Jeu savedJeu = jeuRepository.save(jeu);

        // ACT
        jeuRepository.delete(savedJeu);

        // ASSERT
        Optional<Jeu> deletedJeu = jeuRepository.findById(savedJeu.getId());
        assertThat(deletedJeu).isNotPresent();
    }
}