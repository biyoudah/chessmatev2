package fr.univlorraine.pierreludmannchessmate.repository;

import fr.univlorraine.pierreludmannchessmate.model.Classement;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ClassementRepositoryTest {

    @Autowired
    private ClassementRepository classementRepository;

    @Test
    void should_save_and_find_classement() {
        // ARRANGE : Le nom est la clé primaire
        String classementNom = "Classement ELO Général";
        Classement classement = new Classement(classementNom);

        classementRepository.save(classement);

        Optional<Classement> foundClassement = classementRepository.findById(classementNom);

        assertThat(foundClassement).isPresent();
        assertThat(foundClassement.get().getNom()).isEqualTo(classementNom);
    }

    @Test
    void should_update_classement() {
        // ARRANGE
        String classementNom = "Classement Hebdo";
        Classement classement = new Classement(classementNom);
        classementRepository.save(classement);

        classement.setDateMAJ(classement.getDateMAJ().plusSeconds(10));

        classementRepository.save(classement);

        Optional<Classement> updatedClassement = classementRepository.findById(classementNom);
        assertThat(updatedClassement).isPresent();
        assertThat(updatedClassement.get().getDateMAJ()).isAfter(classement.getDateMAJ().minusSeconds(1));
    }

    @Test
    void should_delete_classement() {
        String classementNom = "Classement Bêta";
        Classement classement = new Classement(classementNom);
        classementRepository.save(classement);

        classementRepository.deleteById(classementNom);

        Optional<Classement> deletedClassement = classementRepository.findById(classementNom);
        assertThat(deletedClassement).isNotPresent();
    }
}