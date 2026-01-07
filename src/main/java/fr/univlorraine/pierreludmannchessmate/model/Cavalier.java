package fr.univlorraine.pierreludmannchessmate.model;

/**
 * Représente la pièce d'échecs Cavalier (Knight).
 *
 * Cette classe hérite de {@link Piece} et implémente la logique spécifique
 * au déplacement et à la représentation graphique du Cavalier.
 */
public class Cavalier extends Piece {

    /**
     * Constructeur de la classe Cavalier.
     *
     * Initialise la pièce en spécifiant sa couleur.
     *
     * @param estBlanc Indique si le Cavalier est blanc ({@code true}) ou noir ({@code false}).
     */
    public Cavalier(boolean estBlanc) {
        super(estBlanc);
    }

    /**
     * Renvoie le symbole Unicode représentant graphiquement le Cavalier.
     *
     * Le symbole dépend de la couleur de la pièce.
     *
     * @return Le caractère Unicode (♘ ou ♞).
     */
    @Override
    public String dessiner() {
        if (estBlanc()) {
            return "\u2658"; // Symbole du Cavalier Blanc (♘)
        } else {
            return "\u265E"; // Symbole du Cavalier Noir (♞)
        }
    }

    /**
     * Vérifie si le déplacement du Cavalier entre deux cases est valide selon les règles des échecs.
     *
     * Le Cavalier se déplace en "L" : deux cases dans une direction (horizontale ou verticale)
     * et une case perpendiculaire à cette direction.
     *
     * @param departLigne Ligne de départ.
     * @param departColonne Colonne de départ.
     * @param arriveeligne Ligne d'arrivée souhaitée.
     * @param arriveColonne Colonne d'arrivée souhaitée.
     * @return {@code true} si le déplacement est un mouvement en "L" (2x1 ou 1x2), {@code false} sinon.
     */
    @Override
    public boolean deplacementValide(int departLigne, int departColonne, int arriveeligne, int arriveColonne) {
        // Calcul de la différence absolue sur les Lignes
        int diffL = Math.abs(arriveeligne - departLigne);

        // Calcul de la différence absolue sur les Colonnes
        int diffC = Math.abs(arriveColonne - departColonne);

        // Le mouvement est valide s'il correspond à un déplacement de 2 cases sur un axe et 1 case sur l'autre.
        return (diffL == 2 && diffC == 1) || (diffL == 1 && diffC == 2);
    }
}