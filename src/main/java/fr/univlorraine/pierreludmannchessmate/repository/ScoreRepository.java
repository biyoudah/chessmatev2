package fr.univlorraine.pierreludmannchessmate.repository;

import fr.univlorraine.pierreludmannchessmate.model.Score;
import fr.univlorraine.pierreludmannchessmate.model.Utilisateur;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScoreRepository extends CrudRepository<Score, Long> {

    boolean existsByUtilisateurAndSchemaKey(Utilisateur utilisateur, String schemaKey);

    @Query("select s.utilisateur.pseudo as pseudo, sum(s.points) as totalPoints from Score s group by s.utilisateur.pseudo order by totalPoints desc")
    List<ClassementRow> getClassementGlobal();

    @Query("select s.utilisateur.pseudo as pseudo, sum(s.points) as totalPoints from Score s where s.mode = :mode group by s.utilisateur.pseudo order by totalPoints desc")
    List<ClassementRow> getClassementParMode(@Param("mode") String mode);

    interface ClassementRow {
        String getPseudo();
        Long getTotalPoints();
    }
}
