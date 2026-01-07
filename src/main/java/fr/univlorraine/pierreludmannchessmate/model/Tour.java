package fr.univlorraine.pierreludmannchessmate.model;

/**
 * Représente la pièce d'échecs Tour (Rook).
 *
 * Cette classe hérite de {@link Piece}. La Tour est une pièce à longue portée
 * qui se déplace exclusivement en ligne droite, horizontalement ou verticalement.
 */
public class Tour extends Piece {

    /**
     * Constructeur de la classe Tour.
     *
     * Initialise la pièce en spécifiant sa couleur.
     *
     * @param estBlanc Indique si la Tour est blanche ({@code true}) ou noire ({@code false}).
     */
    public Tour(boolean estBlanc) {
        super(estBlanc);
    }

    /**
     * Renvoie le symbole Unicode représentant graphiquement la Tour.
     *
     * @return Le caractère Unicode (♖ pour le blanc, ♜ pour le noir).
     */
    @Override
    public String dessiner() {
        if (estBlanc()) {
            return "\u2656"; // Symbole de la Tour Blanche (♖)
        } else {
            return "\u265C"; // Symbole de la Tour Noire (♜)
        }
    }

    /**
     * Vérifie si le déplacement de la Tour est valide géométriquement.
     *
     * La Tour se déplace si la ligne de départ est égale à la ligne d'arrivée (mouvement horizontal)
     * ou si la colonne de départ est égale à la colonne d'arrivée (mouvement vertical).
     *
     * @param departLigne Ligne de départ.
     * @param departColonne Colonne de départ.
     * @param arriveeligne Ligne d'arrivée souhaitée.
     * @param arriveColonne Colonne d'arrivée souhaitée.
     * @return {@code true} si le déplacement est rectiligne, {@code false} sinon.
     */
    @Override
    public boolean deplacementValide(int departLigne, int departColonne,  int arriveeligne, int arriveColonne) {
        // La tour se déplace horizontalement ou verticalement
        // Cela signifie que soit la coordonnée X ne change pas, soit la coordonnée Y ne change pas.
        return (departLigne == arriveeligne || departColonne == arriveColonne);
    }
}