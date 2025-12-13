package fr.univlorraine.pierreludmannchessmate;

/**
 * Représente une case du plateau d'échecs.
 *
 * La classe Case stocke les coordonnées (x, y) de la case,
 * son état (vide ou occupée), et la pièce qu'elle contient
 * si elle n'est pas vide.
 */
public class Case {
    private int x;
    private int y;
    private boolean estvide;
    private Piece piece;

    /**
     * Constructeur de la classe Case.
     *
     * Initialise les coordonnées de la case et son état initial.
     *
     * @param x Coordonnée en abscisse de la case.
     * @param y Coordonnée en ordonnée de la case.
     * @param estvide Indique si la case est initialement vide (true) ou non (false).
     * @param piece La pièce initialement placée sur la case (peut être null).
     */
    public Case(int x, int y, boolean estvide, Piece piece) {
        this.x = x;
        this.y = y;
        this.estvide = estvide;
        this.piece = piece;
    }

    /**
     * Renvoie l'état de la case (vide ou occupée).
     *
     * @return {@code true} si la case est vide, {@code false} sinon.
     */
    public boolean isEstVide() {
        return estvide;
    }

    /**
     * Définit l'état de la case.
     *
     * @param estvide Nouvelle valeur pour l'état de la case (vide ou occupée).
     */
    public void setEstVide(boolean estvide) {
        this.estvide = estvide;
    }

    /**
     * Renvoie la pièce actuellement située sur cette case.
     *
     * @return La pièce (objet Piece) ou {@code null} si la case est vide.
     */
    public Piece getPiece() {
        return piece;
    }

    /**
     * Définit la pièce située sur cette case.
     *
     * Cette méthode met à jour la pièce et devrait être appelée en conjonction avec
     * {@code setEstVide} pour maintenir la cohérence de l'état de la case.
     *
     * @param piece Nouvelle pièce à placer sur la case.
     */
    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    /**
     * Renvoie la coordonnée en abscisse (X) de la case.
     *
     * @return La coordonnée X.
     */
    public int getX() {
        return x;
    }

    /**
     * Renvoie la coordonnée en ordonnée (Y) de la case.
     *
     * @return La coordonnée Y.
     */
    public int getY() {
        return y;
    }
}