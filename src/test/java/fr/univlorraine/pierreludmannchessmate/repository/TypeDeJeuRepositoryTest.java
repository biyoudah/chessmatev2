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
        Long typeId = 1L;
        String nom = "Logique";
        TypeDeJeu type = new TypeDeJeu(typeId, nom);

        typeDeJeuRepository.save(type);
        Optional<TypeDeJeu> foundType = typeDeJeuRepository.findById(typeId);

        assertThat(foundType).isPresent();
        assertThat(foundType.get().getNomTypeDeJeu()).isEqualTo(nom);
    }

    @Test
    void should_update_typeDeJeu() {
        Long typeId = 2L;
        TypeDeJeu type = new TypeDeJeu(typeId, "Mat");
        typeDeJeuRepository.save(type);

        String nouveauNom = "MatEn3Coups";
        type.setNomTypeDeJeu(nouveauNom);
        typeDeJeuRepository.save(type);

        Optional<TypeDeJeu> updatedType = typeDeJeuRepository.findById(typeId);
        assertThat(updatedType).isPresent();
        assertThat(updatedType.get().getNomTypeDeJeu()).isEqualTo(nouveauNom);
    }

    @Test
    void should_delete_typeDeJeu() {
        Long typeId = 3L;
        TypeDeJeu type = new TypeDeJeu(typeId, "MatEn3Coups");
        typeDeJeuRepository.save(type);

        typeDeJeuRepository.deleteById(typeId);

        Optional<TypeDeJeu> deletedType = typeDeJeuRepository.findById(typeId);
        assertThat(deletedType).isNotPresent();
    }
}