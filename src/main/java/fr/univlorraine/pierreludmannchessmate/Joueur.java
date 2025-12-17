package fr.univlorraine.pierreludmannchessmate;

/**
 * Représente un joueur participant à une partie d'échecs.
 *
 * Cette classe gère l'identité du joueur (pseudonyme, authentification) ainsi que
 * son état actuel dans le jeu (couleur des pièces attribuées, statut "en échec").
 */
public class Joueur {
    private String pseudo;
    private String mdp;
    private boolean estBlanc; // true = blancs, false = noirs
    private boolean enEchec; // pour gérer l'état d'échec


    /**
     * Constructeur simplifié.
     *
     * Destiné aux parties locales ou aux tests où l'authentification n'est pas requise.
     * Le mot de passe est défini à {@code null}.
     *
     * @param pseudo Le pseudonyme du joueur.
     * @param estBlanc La couleur des pièces ({@code true} pour les Blancs, {@code false} pour les Noirs).
     */
    public Joueur(String pseudo, boolean estBlanc) {
        this.pseudo = pseudo;
        this.mdp = null;
        this.estBlanc = estBlanc;
        this.enEchec = false;
    }


    /**
     * Renvoie le pseudonyme du joueur.
     *
     * @return Le pseudo sous forme de chaîne de caractères.
     */
    public String getPseudo() {
        return pseudo;
    }

    /**
     * Renvoie le mot de passe du joueur.
     *
     * @return Le mot de passe (peut être null).
     */
    public String getMdp() {
        return mdp;
    }

    /**
     * Indique la couleur des pièces du joueur.
     *
     * @return {@code true} si le joueur joue les Blancs, {@code false} s'il joue les Noirs.
     */
    public boolean estBlanc() {
        return estBlanc;
    }

    /**
     * Vérifie si le Roi du joueur est actuellement en situation d'échec.
     *
     * @return {@code true} si le joueur est en échec, {@code false} sinon.
     */
    public boolean isEnEchec() {
        return enEchec;
    }



    /**
     * Modifie le pseudonyme du joueur.
     *
     * @param pseudo Le nouveau pseudonyme.
     */
    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    /**
     * Modifie le mot de passe du joueur.
     *
     * @param mdp Le nouveau mot de passe.
     */
    public void setMdp(String mdp) {
        this.mdp = mdp;
    }

    /**
     * Met à jour le statut "en échec" du joueur.
     *
     * Cette méthode est appelée par le moteur de jeu après chaque coup de l'adversaire.
     *
     * @param enEchec Nouvel état de sécurité du Roi.
     */
    public void setEnEchec(boolean enEchec) {
        this.enEchec = enEchec;
    }



    /**
     * Vérifie si le mot de passe fourni correspond à celui du joueur.
     *
     * Cette méthode est utile pour les phases de connexion ou de validation sécurisée.
     *
     * @param mdp Le mot de passe à tester.
     * @return {@code true} si le mot de passe est correct, {@code false} sinon (ou si aucun mot de passe n'est défini).
     */
    public boolean verifierMotDePasse(String mdp) {
        return this.mdp != null && this.mdp.equals(mdp);
    }

    /**
     * Renvoie une représentation textuelle du joueur.
     *
     * Utile pour l'affichage dans la console ou les logs de débogage.
     *
     * @return Une chaîne au format "Pseudo (Couleur)".
     */
    @Override
    public String toString() {
        return pseudo + " (" + (estBlanc ? "Blancs" : "Noirs") + ")";
    }
}