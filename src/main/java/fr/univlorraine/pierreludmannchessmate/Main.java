package fr.univlorraine.pierreludmannchessmate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée principal de l'application ChessMate.
 *
 * Cette classe contient la méthode statique {@code main} nécessaire au lancement
 * de l'application Java. L'annotation {@link SpringBootApplication} indique à
 * Spring Boot d'activer l'auto-configuration et le scan des composants dans ce package.
 */
@SpringBootApplication
public class Main {

    /**
     * Méthode de démarrage de l'application.
     *
     * Elle délègue le processus de démarrage à la classe {@link SpringApplication},
     * qui va initialiser le contexte de l'application, démarrer le serveur web embarqué
     * (Tomcat) et charger les configurations définies.
     *
     * @param args Arguments de la ligne de commande passés lors de l'exécution du programme.
     */
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

}