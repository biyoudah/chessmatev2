package fr.univlorraine.pierreludmannchessmate.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import java.time.Instant;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Data
@Entity
public class Utilisateur {
    @Id
    @GeneratedValue
    private Integer id;
    private String pseudo;
    private String email;
    private String motDePasse;
    @CreationTimestamp
    private Instant dateCreation;
    @UpdateTimestamp
    private Instant dateMAJ;

    public Utilisateur() {
    }
    public Utilisateur(String pseudo, String email, String motDePasse) {
        this.pseudo = pseudo;
        this.email = email;
        this.motDePasse = motDePasse;
    }
}


