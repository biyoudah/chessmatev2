package fr.univlorraine.pierreludmannchessmate.model;

/**
 * Représente la pièce d'échecs Fou (Bishop).
 *
 * Cette classe hérite de {@link Piece} et implémente la logique spécifique
 * au déplacement diagonal. En raison de sa nature de déplacement, un Fou
 * reste toujours sur des cases de la même couleur tout au long de la partie.
 */
public class Fou extends Piece {

    /**
     * Constructeur de la classe Fou.
     *
     * Initialise la pièce en spécifiant sa couleur.
     *
     * @param estBlanc Indique si le Fou est blanc ({@code true}) ou noir ({@code false}).
     */
    public Fou(boolean estBlanc) {
        super(estBlanc);
    }

    /**
     * Renvoie le symbole Unicode représentant graphiquement le Fou.
     *
     * @return Le caractère Unicode (♗ pour le blanc, ♝ pour le noir).
     */
    @Override
    public String dessiner() {
        if (estBlanc()) {
            return "\u2657"; // Symbole du Fou Blanc (♗)
        } else {
            return "\u265D"; // Symbole du Fou Noir (♝)
        }
    }

    /**
     * Vérifie si le déplacement du Fou entre deux cases est valide selon les règles.
     *
     * Le Fou se déplace exclusivement en diagonale. Mathématiquement, cela signifie
     * que la valeur absolue de la différence des lignes doit être égale à la
     * valeur absolue de la différence des colonnes (|Δx| = |Δy|).
     *
     * @param departLigne Ligne de départ.
     * @param departColonne Colonne de départ.
     * @param arriveeligne Ligne d'arrivée souhaitée.
     * @param arriveColonne Colonne d'arrivée souhaitée.
     * @return {@code true} si le déplacement est strictement diagonal, {@code false} sinon.
     */
    @Override
    public boolean deplacementValide (int departLigne, int departColonne,  int arriveeligne, int arriveColonne) {
        // Vérification de la diagonale : le déplacement horizontal doit égaler le déplacement vertical
        return (Math.abs(departLigne - arriveeligne) == Math.abs(departColonne -  arriveColonne));
    }
}