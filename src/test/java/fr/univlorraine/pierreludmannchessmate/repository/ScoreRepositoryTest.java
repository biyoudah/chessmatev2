package fr.univlorraine.pierreludmannchessmate.repository;

import fr.univlorraine.pierreludmannchessmate.model.Score;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
// @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ScoreRepositoryTest {

    @Autowired
    private ScoreRepository scoreRepository;

    @Test
    void should_save_and_find_score() {
        Score score = new Score(120, true); // 120 secondes, réussi

        Score savedScore = scoreRepository.save(score);
        Optional<Score> foundScore = scoreRepository.findById(savedScore.getId());

        assertThat(foundScore).isPresent();
        assertThat(foundScore.get().getTempsResolution()).isEqualTo(120);
        assertThat(foundScore.get().isReussi()).isTrue();
    }

    @Test
    void should_update_score() {
        Score score = new Score(300, false);
        scoreRepository.save(score);

        int nouveauTemps = 90;
        boolean nouveauReussi = true;

        score.setTempsResolution(nouveauTemps);
        score.setReussi(nouveauReussi);
        scoreRepository.save(score); // Sauvegarde l'objet modifié

        Optional<Score> updatedScore = scoreRepository.findById(score.getId());

        assertThat(updatedScore).isPresent();
        assertThat(updatedScore.get().getTempsResolution()).isEqualTo(nouveauTemps);
        assertThat(updatedScore.get().isReussi()).isEqualTo(nouveauReussi);
    }

    @Test
    void should_delete_score() {
        Score score = new Score(500, false);
        Score savedScore = scoreRepository.save(score);

        scoreRepository.delete(savedScore);

        Optional<Score> deletedScore = scoreRepository.findById(savedScore.getId());

        assertThat(deletedScore).isNotPresent();
    }
}