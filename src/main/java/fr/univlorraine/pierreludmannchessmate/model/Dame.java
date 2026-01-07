package fr.univlorraine.pierreludmannchessmate.model;

/**
 * Représente la pièce d'échecs Dame (Queen).
 *
 * Cette classe hérite de {@link Piece} et implémente la logique spécifique
 * au déplacement et à la représentation graphique de la Dame.
 * La Dame est la pièce la plus mobile du jeu, combinant les capacités de la Tour et du Fou.
 */
public class Dame extends Piece {

    /**
     * Constructeur de la classe Dame.
     *
     * Initialise la pièce en spécifiant sa couleur.
     *
     * @param estBlanc Indique si la Dame est blanche ({@code true}) ou noire ({@code false}).
     */
    public Dame(boolean estBlanc) {
        super(estBlanc);
    }

    /**
     * Renvoie le symbole Unicode représentant graphiquement la Dame.
     *
     * Le symbole retourné dépend de la couleur définie lors de l'initialisation.
     *
     * @return Le caractère Unicode (♕ pour blanc, ♛ pour noir).
     */
    @Override
    public String dessiner() {
        if (estBlanc()) {
            return "\u2655"; // Symbole de la Dame Blanche (♕)
        } else {
            return "\u265B"; // Symbole de la Dame Noire (♛)
        }
    }

    /**
     * Vérifie si le déplacement de la Dame entre deux cases est valide selon les règles.
     *
     * La Dame peut se déplacer horizontalement ou verticalement (comme une Tour),
     * ou bien en diagonale (comme un Fou). Cette méthode vérifie simplement la géométrie du mouvement
     * sans prendre en compte les obstacles éventuels sur le chemin.
     *
     * @param departLigne Ligne de départ.
     * @param departColonne Colonne de départ.
     * @param arriveeligne Ligne d'arrivée souhaitée.
     * @param arriveColonne Colonne d'arrivée souhaitée.
     * @return {@code true} si le déplacement correspond à une ligne droite ou une diagonale, {@code false} sinon.
     */
    @Override
    public boolean deplacementValide(int departLigne, int departColonne,  int arriveeligne, int arriveColonne) {
        // Vérification du mouvement diagonal (comme le Fou) : la différence de lignes égale la différence de colonnes
        boolean diagonale = Math.abs(arriveeligne - departLigne) == Math.abs(arriveColonne - departColonne);

        // Vérification du mouvement rectiligne (comme la Tour) : même ligne ou même colonne
        boolean ligneOuColonne = (departLigne == arriveeligne || departColonne == arriveColonne);

        // Le mouvement est valide si l'une des deux conditions est remplie
        return diagonale || ligneOuColonne;
    }
}