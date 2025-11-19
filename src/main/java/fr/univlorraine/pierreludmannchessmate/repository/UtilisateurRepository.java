package fr.univlorraine.pierreludmannchessmate.repository;

import fr.univlorraine.pierreludmannchessmate.model.Utilisateur;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UtilisateurRepository extends CrudRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByEmail(String email);
}
