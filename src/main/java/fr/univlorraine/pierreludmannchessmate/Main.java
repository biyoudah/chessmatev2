package fr.univlorraine.pierreludmannchessmate;

public class Main {
    public static void main(String[] args) {
        Fou fouBlanc = new Fou(true);
        fouBlanc.dessiner();
        System.out.println(fouBlanc.deplacementValide(2, 2, 5, 5)); // true
        System.out.println(fouBlanc.deplacementValide(2, 2, 5, 4)); // false
    }
}
