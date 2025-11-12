package fr.univlorraine.pierreludmannchessmate;

import java.util.Date;

public class Score {
    private long id;
    private Joueur joueur;
    private Defi defi;
    private int points;
    private int tempsResolution;
    private Date date;
    private boolean estValide;
    private Echiquier echiquier;

    public Score(Joueur joueur, Defi defi, int points, int tempsResolution, Date date) {
        this.joueur = joueur;
        this.defi = defi;
        this.points = points;
        this.tempsResolution = tempsResolution;
        this.estValide = false;
    }

    public long getId() {
        return id;
    }
    public Joueur getJoueur() {
        return joueur;
    }
    public Defi getDefi() {
        return defi;
    }
    public int getPoints() {
        return points;
    }
    public int getTempsResolution() {
        return tempsResolution;
    }
    public Date getDate() {
        return date;
    }
    public boolean estValide() {
        return estValide;
    }
    public void enregistrer(){
        if (defi.validerSolution(echiquier)){
            points += defi.calculerScore(tempsResolution);
        }
    }
    public void invalider(){
        estValide = false;
    }
}
