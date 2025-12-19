package fr.univlorraine.pierreludmannchessmate;

/**
 * Représente le plateau de jeu d'échecs (l'échiquier).
 *
 * Cette classe gère la grille de 8x8 cases, la disposition des pièces,
 * ainsi que la validation globale des déplacements (bornes, obstacles sur le chemin).
 */
public class Echiquier {
    /**
     * Matrice bidimensionnelle représentant la grille de cases du jeu.
     */
    private final Case[][] e;

    /**
     * Constructeur de la classe Echiquier.
     *
     * Initialise un échiquier vide de dimensions 8x8 et instancie toutes les cases via la méthode {@code initialiser()}.
     */
    public Echiquier(){
        e = new Case[8][8];
        initialiser();
    }

    /**
     * Initialise la grille de jeu.
     *
     * Parcourt la matrice et crée un nouvel objet {@link Case} pour chaque coordonnée (i, j).
     * Les cases sont initialement vides.
     */
    public void initialiser(){
        for (int i = 0; i < 8; i++){
            for (int j = 0; j < 8; j++){
                e[i][j] = new Case(i,j,true,null);
            }
        }
    }

    /**
     * Renvoie l'objet Case situé aux coordonnées spécifiées.
     *
     * @param x Coordonnée en abscisse (Ligne).
     * @param y Coordonnée en ordonnée (Colonne).
     * @return La case correspondante.
     */
    public Case getCase(int x, int y){
        return e[x][y];
    }

    /**
     * Vérifie si les coordonnées sont dans les limites du plateau.
     *
     * Le plateau est indexé de 0 à 7.
     *
     * @param x Coordonnée en abscisse.
     * @param y Coordonnée en ordonnée.
     * @return {@code true} si (x,y) est dans [0,7], {@code false} sinon.
     */
    private boolean coordonneesValides(int x, int y){
        return x >= 0 && x < 8 && y >= 0 && y < 8;
    }

    /**
     * Vérifie si le chemin entre deux cases est libre de tout obstacle.
     *
     * Cette méthode calcule la direction du mouvement (horizontal, vertical ou diagonal)
     * et itère case par case pour s'assurer qu'aucune pièce ne bloque le passage.
     *
     * @param x1 Abscisse de départ.
     * @param y1 Ordonnée de départ.
     * @param x2 Abscisse d'arrivée.
     * @param y2 Ordonnée d'arrivée.
     * @param piece La pièce qui tente de se déplacer (inutilisée dans la logique actuelle mais disponible pour extensions).
     * @return {@code true} si le chemin est libre, {@code false} si une pièce bloque le passage.
     */
    private boolean cheminlibre(int x1, int y1, int x2, int y2, Piece piece){

        // Détermine la direction du pas (-1, 0, ou 1) pour l'axe X et Y
        int deltaX = Integer.compare(x2, x1); // plus petit = -1, 0, plus grand = 1
        int deltaY = Integer.compare(y2, y1); // -1, 0, 1

        int i = x1 + deltaX;
        int j = y1 + deltaY;

        // Parcours des cases intermédiaires jusqu'à la case cible
        while (i != x2 || j != y2) {
            if (e[i][j].getPiece() != null) {
                return false; // Une pièce bloque le chemin
            }
            i += deltaX;
            j += deltaY;
        }

        return true;
    }

    /**
     * Tente de déplacer une pièce d'une case à une autre.
     *
     * Cette méthode effectue une série de validations :
     * 1. Validité des coordonnées.
     * 2. Présence d'une pièce au départ.
     * 3. Validité du mouvement spécifique à la pièce (via {@code piece.deplacementValide}).
     * 4. Absence d'obstacles sur le chemin (via {@code cheminlibre}).
     *
     * Si toutes les conditions sont réunies, le déplacement est effectué et l'état des cases est mis à jour.
     *
     * @param x1 Ligne de départ.
     * @param y1 Colonne de départ.
     * @param x2 Ligne d'arrivée.
     * @param y2 Colonne d'arrivée.
     * @return {@code true} si le déplacement a été effectué avec succès, {@code false} sinon.
     */
    public boolean deplacerPiece(int x1, int y1, int x2, int y2) {
        Piece piece = e[x1][y1].getPiece();

        // Validation des bornes
        if (!coordonneesValides(x1, y1) || !coordonneesValides(x2, y2)) {
            return false;
        }

        // Vérification qu'il y a bien une pièce à bouger
        if (piece == null) return false;

        Case caseDepart = e[x1][y1];
        Case caseArrivee = e[x2][y2];

        // Vérification des règles de déplacement propres à la pièce
        if (!piece.deplacementValide(x1, y1, x2, y2)) {
            System.out.println("Déplacement non valide pour cette pièce !");
            return false;
        }
        // Vérification des obstacles
        if (!cheminlibre(x1, y1, x2, y2, piece)) {
            System.out.println("Le chemin est bloqué !");
            return false;
        }

        // Application du déplacement
        caseArrivee.setPiece(piece);
        caseDepart.setPiece(null);
        caseDepart.setEstVide(true);
        caseArrivee.setEstVide(false);

        System.out.println("Déplacement effectué !");
        return true;
    }


    /**
     * Place une pièce sur une case donnée si celle-ci est vide.
     *
     * Utilitaire principalement utilisé pour l'initialisation de parties ou de défis.
     *
     * @param x1 Ligne de la case.
     * @param y1 Colonne de la case.
     * @param piece La pièce à placer.
     */
    public void placerPiece(int x1, int y1, Piece piece) {
        if(e[x1][y1].isEstVide()){
            e[x1][y1].setPiece(piece);
            e[x1][y1].setEstVide(false);
        }
    }

    /**
     * Retire une pièce d'une case donnée.
     *
     * La case redevient vide.
     *
     * @param x Ligne de la case.
     * @param y Colonne de la case.
     */
    public void retirerPiece(int x, int y) {
        e[x][y].setPiece(null);
        e[x][y].setEstVide(true);
    }


    /**
     * Affiche une représentation textuelle de l'échiquier dans la console.
     *
     * Utilise les symboles Unicode des pièces ou un point (.) pour les cases vides.
     */
    public void afficher() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece p = e[i][j].getPiece();
                System.out.print((p == null ? "." : p.dessiner()) + " ");
            }
            System.out.println();
        }
    }

    /**
     * Renvoie la taille du côté de l'échiquier.
     *
     * @return La taille (normalement 8).
     */
    public int getTaille() {
        return e.length;
    }

    /**
     * Génère une représentation de l'échiquier sous forme de matrice de chaînes de caractères.
     *
     * Cette méthode est destinée à être utilisée par l'interface graphique pour afficher
     * les symboles des pièces aux bons endroits.
     *
     * @return Un tableau 2D de Strings contenant les symboles Unicode des pièces.
     */
    public String[][] getBoardUnicode() {
        // Thymeleaf attend une matrice sous la forme tableau[LIGNE][COLONNE]
        String[][] boardUnicode = new String[8][8];

        for (int col = 0; col < 8; col++) {        // x = Colonne (A..H)
            for (int row = 0; row < 8; row++) {    // y = Ligne (1..8)

                // On récupère la pièce stockée en interne à (col, row)
                // Adaptez 'e[col][row]' ou 'e[row][col]' selon votre déclaration 'Case[][] e'
                // Si vous avez verticalisé les pions, c'est que l'un est l'inverse de l'autre.

                // HYPOTHÈSE FORTE : Votre 'e' est en format e[colonne][ligne]
                Piece p = e[row][col].getPiece();

                // IMPORTANT : On remplit le tableau de vue en inversant : [row][col]
                boardUnicode[row][col] = (p == null) ? "" : p.dessiner();
            }
        }
        return boardUnicode;
    }

}