package fr.univlorraine.pierreludmannchessmate.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Score {
    @Id
    @GeneratedValue
    private Integer id;
    private int tempsResolution;
    private boolean reussi;

    public Score() {
        }

    public Score(int id, int tempsResolution, boolean reussi) {
        this.id = id;
        this.tempsResolution = tempsResolution;
        this.reussi = reussi;
    }
}
