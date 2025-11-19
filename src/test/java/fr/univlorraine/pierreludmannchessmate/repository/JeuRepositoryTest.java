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
        Jeu jeu = new Jeu("Jeu 1", "{}", Difficulte.facile);

        Jeu savedJeu = jeuRepository.save(jeu);
        Optional<Jeu> foundJeu = jeuRepository.findById(savedJeu.getId());

        assertThat(foundJeu).isPresent();
        assertThat(foundJeu.get().getNom()).isEqualTo("Jeu 1");
        assertThat(foundJeu.get().getDifficulte()).isEqualTo(Difficulte.facile);
    }

    @Test
    void should_update_jeu() {
        Jeu jeu = new Jeu("Jeu 2", "{}", Difficulte.normal);
        jeuRepository.save(jeu);

        String nouveauNom = "Partie Modifiée";
        jeu.setNom(nouveauNom);
        jeuRepository.save(jeu);

        Optional<Jeu> updatedJeu = jeuRepository.findById(jeu.getId());
        assertThat(updatedJeu).isPresent();
        assertThat(updatedJeu.get().getNom()).isEqualTo(nouveauNom);
    }

    @Test
    void should_delete_jeu() {
        Jeu jeu = new Jeu("Jeu 2", "{}", Difficulte.facile);
        Jeu savedJeu = jeuRepository.save(jeu);

        jeuRepository.delete(savedJeu);

        Optional<Jeu> deletedJeu = jeuRepository.findById(savedJeu.getId());
        assertThat(deletedJeu).isNotPresent();
    }
}