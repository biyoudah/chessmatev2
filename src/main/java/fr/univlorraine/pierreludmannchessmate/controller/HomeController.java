package fr.univlorraine.pierreludmannchessmate.controller;

import fr.univlorraine.pierreludmannchessmate.repository.UtilisateurRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final UtilisateurRepository utilisateurRepository;

    public HomeController(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    /**
     * Redirection automatique de la racine vers /home.
     */
    @GetMapping("/")
    public String root() {
        return "redirect:/home";
    }

    /**
     * Affiche la page d'accueil (Dashboard).
     * C'est ici que l'utilisateur choisit entre "Bac à sable" et "JeuPuzzle".
     */
    @GetMapping("/home")
    public String home(Model model, Authentication auth) {
        // 1. Injecter les infos utilisateur (Pseudo, Statut connecté)
        injecterInfosUtilisateur(model, auth);

        return "home"; // Correspond à resources/templates/home.html
    }

    /**
     * Méthode utilitaire pour vérifier si l'utilisateur est connecté
     * et passer son pseudo à la vue.
     */
    private void injecterInfosUtilisateur(Model model, Authentication authentication) {
        boolean isConnected = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);

        model.addAttribute("isLoggedIn", isConnected);
        model.addAttribute("pseudo", isConnected ? authentication.getName() : "Invité");
    }
}