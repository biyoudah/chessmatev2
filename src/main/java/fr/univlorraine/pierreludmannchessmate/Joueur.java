package fr.univlorraine.pierreludmannchessmate;

public class Joueur {
    private String pseudo;
    private String mdp;

    public Joueur(String pseudo, String mdp){
        this.pseudo=pseudo;
        this.mdp=mdp;
    }
    public String  getPseudo() {
        return pseudo;
    }
}
