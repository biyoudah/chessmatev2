package fr.univlorraine.pierreludmannchessmate.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Score {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int tempsResolution;
    private boolean reussi;

    public Score() {
        }

    public Score(int tempsResolution, boolean reussi) {
        this.tempsResolution = tempsResolution;
        this.reussi = reussi;
    }
}
