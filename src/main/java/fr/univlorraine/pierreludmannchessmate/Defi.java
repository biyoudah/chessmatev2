package fr.univlorraine.pierreludmannchessmate;

import lombok.Getter;
import java.util.Date;

public abstract class Defi {
    @Getter
    private final long id;
    @Getter
    private final String nom;
    @Getter
    private final String description;
    @Getter
    private final int difficulte;
    @Getter
    private final Date dateCreation;
    @Getter
    private final Joueur createur;

    private static long compteurId = 0;

    public Defi(String nom, String description, int difficulte, Joueur createur) {
        this.id = ++compteurId; // auto-increment id simplifié
        this.nom = nom;
        this.description = description;
        this.difficulte = difficulte;
        this.createur = createur;
        this.dateCreation = new Date();
    }

    /**
     * Valide la solution associée au défi.
     * @param echiquier L'échiquier représentant l'état du défi.
     * @return true si la solution est valide, false sinon.
     */
    public abstract boolean validerSolution(Echiquier echiquier);
}
