package fr.univlorraine.pierreludmannchessmate.repository;

import fr.univlorraine.pierreludmannchessmate.model.TypeDeJeu;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TypeDeJeuRepositoryTest {

    @Autowired
    private TypeDeJeuRepository typeDeJeuRepository;

    @Test
    void should_save_and_find_typeDeJeu() {
        // ARRANGE : L'ID est la clé primaire fournie
        Long typeId = 1L;
        String nom = "Classique";
        TypeDeJeu type = new TypeDeJeu(typeId, nom);

        // ACT
        typeDeJeuRepository.save(type);
        Optional<TypeDeJeu> foundType = typeDeJeuRepository.findById(typeId);

        // ASSERT
        assertThat(foundType).isPresent();
        assertThat(foundType.get().getNomTypeDeJeu()).isEqualTo(nom);
    }

    @Test
    void should_update_typeDeJeu() {
        // ARRANGE
        Long typeId = 2L;
        TypeDeJeu type = new TypeDeJeu(typeId, "Blitz");
        typeDeJeuRepository.save(type);

        // ACT
        String nouveauNom = "Bullet";
        type.setNomTypeDeJeu(nouveauNom);
        typeDeJeuRepository.save(type);

        // ASSERT
        Optional<TypeDeJeu> updatedType = typeDeJeuRepository.findById(typeId);
        assertThat(updatedType).isPresent();
        assertThat(updatedType.get().getNomTypeDeJeu()).isEqualTo(nouveauNom);
    }

    @Test
    void should_delete_typeDeJeu() {
        // ARRANGE
        Long typeId = 3L;
        TypeDeJeu type = new TypeDeJeu(typeId, "Rapide");
        typeDeJeuRepository.save(type);

        // ACT
        typeDeJeuRepository.deleteById(typeId);

        // ASSERT
        Optional<TypeDeJeu> deletedType = typeDeJeuRepository.findById(typeId);
        assertThat(deletedType).isNotPresent();
    }
}