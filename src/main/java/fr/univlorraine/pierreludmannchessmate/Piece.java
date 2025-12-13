package fr.univlorraine.pierreludmannchessmate;

/**
 * Classe abstraite représentant une pièce d'échecs générique.
 *
 * Cette classe définit le contrat commun à toutes les pièces du jeu (Roi, Dame, Pion, etc.).
 * Elle encapsule la propriété fondamentale de couleur (Blanc ou Noir) et impose aux sous-classes
 * d'implémenter leur propre logique de déplacement et d'affichage graphique.
 */
public abstract class Piece {

    /**
     * Attribut déterminant la couleur de la pièce.
     * Vrai pour les Blancs, Faux pour les Noirs.
     */
    private boolean estBlanc;

    /**
     * Constructeur de la classe Piece.
     *
     * Initialise la pièce en définissant son appartenance à l'un des deux camps.
     *
     * @param estBlanc Indique si la pièce est blanche ({@code true}) ou noire ({@code false}).
     */
    public Piece(boolean estBlanc){
        this.estBlanc = estBlanc;
    }

    /**
     * Renvoie la couleur de la pièce.
     *
     * @return {@code true} si la pièce est blanche, {@code false} si elle est noire.
     */
    public boolean estBlanc(){
        return estBlanc;
    }

    /**
     * Vérifie la validité géométrique d'un déplacement pour cette pièce spécifique.
     *
     * Cette méthode est abstraite car chaque type de pièce possède ses propres règles de mouvement
     * (ex: diagonale pour le Fou, rectiligne pour la Tour). L'implémentation ne doit vérifier
     * que la "forme" du mouvement, sans nécessairement se soucier des obstacles (géré par l'Echiquier).
     *
     * @param departLigne Ligne de la case de départ.
     * @param departColonne Colonne de la case de départ.
     * @param arriveeligne Ligne de la case d'arrivée.
     * @param arriveColonne Colonne de la case d'arrivée.
     * @return {@code true} si le mouvement respecte les règles de la pièce, {@code false} sinon.
     */
    public abstract boolean deplacementValide (int departLigne, int departColonne,  int arriveeligne, int arriveColonne);

    /**
     * Renvoie la représentation graphique de la pièce.
     *
     * Cette méthode abstraite doit être implémentée par les sous-classes pour retourner
     * le symbole Unicode correspondant à la pièce (ex: ♔, ♙, etc.) selon sa couleur.
     *
     * @return Une chaîne de caractères contenant le symbole de la pièce.
     */
    public abstract String dessiner();

}