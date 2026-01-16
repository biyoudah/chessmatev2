package fr.univlorraine.pierreludmannchessmate.repository;

import fr.univlorraine.pierreludmannchessmate.logic.JeuPuzzle;
import fr.univlorraine.pierreludmannchessmate.model.Puzzle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PuzzleRepository extends JpaRepository<Puzzle, String> {

    // Requête pour récupérer un puzzle aléatoire selon la difficulté (nombre de coups)
    // On calcule le nombre de coups en comptant les espaces dans la colonne 'moves'
    @Query(value = "SELECT * FROM puzzle WHERE " +
            "(LENGTH(moves) - LENGTH(REPLACE(moves, ' ', '')) + 1) BETWEEN :minMoves AND :maxMoves " +
            "ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<Puzzle> findRandomByMoveCount(@Param("minMoves") int minMoves, @Param("maxMoves") int maxMoves);

    Optional<Puzzle> findByPuzzleId(String id);
}