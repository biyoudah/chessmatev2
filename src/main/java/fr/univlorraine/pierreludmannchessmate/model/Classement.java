package fr.univlorraine.pierreludmannchessmate.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Data
@Entity
public class Classement {
    @Id
    private String nom;
    @UpdateTimestamp
    private Instant dateMAJ;

    public Classement() {
    }

    public Classement(String nom) {
        this.nom = nom;
        this.dateMAJ = Instant.now();
    }
}
