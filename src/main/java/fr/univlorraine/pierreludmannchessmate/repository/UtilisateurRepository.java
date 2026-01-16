    package fr.univlorraine.pierreludmannchessmate.repository;

    import fr.univlorraine.pierreludmannchessmate.model.Utilisateur;
    import jakarta.transaction.Transactional;
    import org.springframework.data.jpa.repository.Modifying;
    import org.springframework.data.jpa.repository.Query;
    import org.springframework.data.repository.CrudRepository;
    import org.springframework.data.repository.query.Param;

    import java.util.Optional;

    public interface UtilisateurRepository extends CrudRepository<Utilisateur, Long> {
        Optional<Utilisateur> findByEmail(String email);
        Optional<Utilisateur> findByPseudo(String pseudo);
        // UtilisateurRepository.java
        @Transactional // Indispensable pour les requêtes @Modifying
        @Modifying
        @Query("UPDATE Utilisateur u SET u.tempsTotalDeJeu = u.tempsTotalDeJeu + :secondes WHERE u.id = :id")
        void ajouterTempsDeJeu(@Param("id") Long id, @Param("secondes") long secondes);
        }

