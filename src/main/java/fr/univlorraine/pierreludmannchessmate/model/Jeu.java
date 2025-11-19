package fr.univlorraine.pierreludmannchessmate.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Jeu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private String donneesProbleme;
    private Difficulte difficulte;

    public Jeu() {
    }
    public Jeu(String nom,String donnees,Difficulte diff) {
        this.nom = nom;
        this.donneesProbleme = donnees;
        this.difficulte = diff;
    }
}
