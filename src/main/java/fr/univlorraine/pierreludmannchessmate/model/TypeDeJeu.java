package fr.univlorraine.pierreludmannchessmate.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class TypeDeJeu {
    @Id
    private Integer id;
    private String nomTypeDeJeu;

    public TypeDeJeu() {
    }
    public TypeDeJeu(Integer id,String nomTypeDeJeu) {
        this.id = id;
        this.nomTypeDeJeu = nomTypeDeJeu;
    }
}
