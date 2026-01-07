package fr.univlorraine.pierreludmannchessmate.model;

/**
 * Représente la pièce d'échecs Roi (King).
 *
 * Cette classe hérite de {@link Piece}. C'est la pièce la plus importante du jeu ;
 * bien que sa portée soit limitée, sa capture (échec et mat) signifie la fin de la partie.
 */
public class Roi extends Piece {

    /**
     * Constructeur de la classe Roi.
     *
     * Initialise la pièce en spécifiant sa couleur.
     *
     * @param estBlanc Indique si le Roi est blanc ({@code true}) ou noir ({@code false}).
     */
    public Roi(boolean estBlanc) {
        super(estBlanc);
    }

    /**
     * Renvoie le symbole Unicode représentant graphiquement le Roi.
     *
     * @return Le caractère Unicode (♔ pour le blanc, ♚ pour le noir).
     */
    @Override
    public String dessiner() {
        if (estBlanc()) {
            return "\u2654"; // Symbole du Roi Blanc (♔)
        } else {
            return "\u265A"; // Symbole du Roi Noir (♚)
        }
    }

    /**
     * Vérifie si le déplacement du Roi est valide.
     *
     * Le Roi peut se déplacer d'une seule case dans n'importe quelle direction
     * (horizontale, verticale ou diagonale).
     *
     * @param departLigne Ligne de départ.
     * @param departColonne Colonne de départ.
     * @param arriveeligne Ligne d'arrivée souhaitée.
     * @param arriveColonne Colonne d'arrivée souhaitée.
     * @return {@code true} si le déplacement est d'exactement une case autour de la position actuelle, {@code false} sinon.
     */
    @Override
    public boolean deplacementValide(int departLigne, int departColonne,  int arriveeligne, int arriveColonne) {
        int diffL = Math.abs(arriveeligne - departLigne);
        int diffC = Math.abs(arriveColonne - departColonne);

        // Le roi se déplace d'une case max dans n'importe quelle direction
        // La condition !(diffL == 0 && diffC == 0) s'assure qu'on ne reste pas sur place
        return (diffL <= 1 && diffC <= 1) && !(diffL == 0 && diffC == 0);
    }
}