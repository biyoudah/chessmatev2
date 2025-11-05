package fr.univlorraine.pierreludmannchessmate;

public class Joueur {
    private String pseudo;
    private String mdp;
    private boolean estBlanc; // true = blancs, false = noirs
    private boolean enEchec; // pour gérer l'état d'échec

    // Constructeur pour le jeu web (avec authentification)
    public Joueur(String pseudo, String mdp, boolean estBlanc) {
        this.pseudo = pseudo;
        this.mdp = mdp;
        this.estBlanc = estBlanc;
        this.enEchec = false;
    }

    // Constructeur simplifié (sans authentification pour l'instant)
    public Joueur(String pseudo, boolean estBlanc) {
        this.pseudo = pseudo;
        this.mdp = null;
        this.estBlanc = estBlanc;
        this.enEchec = false;
    }

    // Getters
    public String getPseudo() {
        return pseudo;
    }

    public String getMdp() {
        return mdp;
    }

    public boolean estBlanc() {
        return estBlanc;
    }

    public boolean isEnEchec() {
        return enEchec;
    }

    // Setters
    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public void setMdp(String mdp) {
        this.mdp = mdp;
    }

    public void setEnEchec(boolean enEchec) {
        this.enEchec = enEchec;
    }

    // Méthode pour vérifier l'authentification (si nécessaire plus tard)
    public boolean verifierMotDePasse(String mdp) {
        return this.mdp != null && this.mdp.equals(mdp);
    }

    @Override
    public String toString() {
        return pseudo + " (" + (estBlanc ? "Blancs" : "Noirs") + ")";
    }
}
