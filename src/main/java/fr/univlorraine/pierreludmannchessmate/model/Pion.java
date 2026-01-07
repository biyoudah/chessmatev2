package fr.univlorraine.pierreludmannchessmate.model;

/**
 * Représente la pièce d'échecs Pion (Pawn).
 *
 * Cette classe hérite de {@link Piece}. Le Pion est la seule pièce qui ne peut pas reculer.
 * Sa direction de déplacement dépend de sa couleur (les Blancs "montent" vers les indices décroissants,
 * les Noirs "descendent" vers les indices croissants).
 */
public class Pion extends Piece {

    /**
     * Constructeur de la classe Pion.
     *
     * Initialise la pièce en spécifiant sa couleur.
     *
     * @param estBlanc Indique si le Pion est blanc ({@code true}) ou noir ({@code false}).
     */
    public Pion(boolean estBlanc) {
        super(estBlanc);
    }

    /**
     * Renvoie le symbole Unicode représentant graphiquement le Pion.
     *
     * @return Le caractère Unicode (♙ pour le blanc, ♟ pour le noir).
     */
    @Override
    public String dessiner() {
        if (estBlanc()) {
            return "\u2659"; // Symbole du Pion Blanc (♙)
        } else {
            return "\u265F"; // Symbole du Pion Noir (♟)
        }
    }

    /**
     * Vérifie si le déplacement du Pion est valide.
     *
     * Le Pion se déplace verticalement (changement de ligne seulement).
     *
     * <p><strong>Note :</strong> Cette implémentation gère uniquement le mouvement
     * d'avance d'une case. Les règles de prise en diagonale et du double pas initial
     * ne sont pas incluses ici.</p>
     *
     * @param departLigne Ligne de départ.
     * @param departColonne Colonne de départ.
     * @param arriveeligne Ligne d'arrivée souhaitée.
     * @param arriveColonne Colonne d'arrivée souhaitée.
     * @return {@code true} si le pion avance d'une case dans sa direction respective, {@code false} sinon.
     */
    @Override
    public boolean deplacementValide(int departLigne, int departColonne,  int arriveeligne, int arriveColonne) {
        int diffL = arriveeligne - departLigne;
        // Le déplacement latéral est interdit pour un mouvement simple (diffC doit être 0)
        int diffC = Math.abs(arriveColonne - departColonne);

        if (estBlanc()) {
            // Le Pion blanc monte vers le haut du tableau (indices décroissants : ex. ligne 6 vers 5)
            return (diffL == -1 && diffC == 0); // avance d'une case tout droit
        } else {
            // Le Pion noir descend vers le bas du tableau (indices croissants : ex. ligne 1 vers 2)
            return (diffL == 1 && diffC == 0);
        }
    }
}