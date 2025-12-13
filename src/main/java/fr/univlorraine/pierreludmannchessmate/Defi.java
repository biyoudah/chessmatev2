package fr.univlorraine.pierreludmannchessmate;

import lombok.Getter;
import java.util.Date;

/**
 * Classe abstraite représentant un défi d'échecs générique.
 *
 * Cette classe sert de base à tous les types de défis (ex: Mat en N coups, étude de finale).
 * Elle stocke les métadonnées communes (nom, difficulté, créateur) et définit le contrat
 * que les sous-classes doivent respecter pour la validation et le score.
 */
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

    /**
     * Constructeur de la classe abstraite Defi.
     *
     * Initialise un nouveau défi avec ses informations de base et lui attribue
     * un identifiant unique via un compteur statique auto-incrémenté.
     * La date de création est fixée à l'instant de l'instanciation.
     *
     * @param nom Le nom ou titre du défi.
     * @param description Une brève description ou consigne du défi.
     * @param difficulte Niveau de difficulté du défi (ex: 1 pour facile, 5 pour difficile).
     * @param createur Le joueur qui a créé ce défi.
     */
    public Defi(String nom, String description, int difficulte, Joueur createur) {
        this.id = ++compteurId; // auto-increment id simplifié
        this.nom = nom;
        this.description = description;
        this.difficulte = difficulte;
        this.createur = createur;
        this.dateCreation = new Date();
    }

    /**
     * Vérifie si l'état actuel de l'échiquier constitue une solution valide au défi.
     *
     * Cette méthode est abstraite et doit être implémentée par les classes filles
     * pour définir les conditions de victoire spécifiques (ex: le roi est-il mat ?).
     *
     * @param echiquier L'échiquier représentant l'état final proposé par le joueur.
     * @return true si la solution est valide, false sinon.
     */
    public abstract boolean validerSolution(Echiquier echiquier);

    /**
     * Calcule le score obtenu par le joueur pour la résolution de ce défi.
     *
     * Le calcul prend en compte le temps passé pour trouver la solution.
     * Les critères exacts de points dépendent de l'implémentation dans les sous-classes.
     *
     * @param tempsResolution Le temps (en secondes ou millisecondes) mis pour résoudre le défi.
     * @return Le score calculé sous forme d'entier.
     */
    public abstract int calculerScore(int tempsResolution);
}