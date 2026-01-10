package fr.univlorraine.pierreludmannchessmate.repository;

import fr.univlorraine.pierreludmannchessmate.model.Score;
import fr.univlorraine.pierreludmannchessmate.model.Utilisateur;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface ScoreRepository extends CrudRepository<Score, Long> {

    boolean existsByUtilisateurAndSchemaKey(Utilisateur utilisateur, String schemaKey);

    @Query("select s.schemaKey from Score s where s.utilisateur = :user and s.reussi = true")
    Set<String> findCompletedSchemaKeysByUser(@Param("user") Utilisateur user);

    long countByUtilisateurAndReussiTrue(Utilisateur user);

    long countByUtilisateurAndPerfectTrue(Utilisateur user);

    long countByUtilisateurAndModeAndReussiTrue(Utilisateur user, String mode);

    @Query("select s.utilisateur.pseudo as pseudo, sum(s.points) as totalPoints from Score s group by s.utilisateur.pseudo order by totalPoints desc, min(s.createdAt) asc")
    List<ClassementRow> getClassementGlobal();

    @Query("select s.utilisateur.pseudo as pseudo, sum(s.points) as totalPoints from Score s where s.mode = :mode group by s.utilisateur.pseudo order by totalPoints desc, min(s.createdAt) asc")
    List<ClassementRow> getClassementParMode(@Param("mode") String mode);

    boolean existsBySchemaKeyAndReussiTrue(String schemaKey);

    interface ClassementRow {
        String getPseudo();
        Long getTotalPoints();
    }
}