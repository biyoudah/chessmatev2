package fr.univlorraine.pierreludmannchessmate.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GenerationType;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String pseudo;
    private String email;
    private String password;
    @CreationTimestamp
    private Instant dateCreation;
    @UpdateTimestamp
    private Instant dateMAJ;
    private String role; // Ex: "USER", "ADMIN"
    private long tempsTotalDeJeu; // En secondes

    public Utilisateur() {
    }
    public Utilisateur(String pseudo, String email, String password) {
        this.pseudo = pseudo;
        this.email = email;
        this.password = password;
    }
}


