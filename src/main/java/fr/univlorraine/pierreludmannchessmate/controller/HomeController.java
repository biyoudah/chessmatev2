package fr.univlorraine.pierreludmannchessmate.controller;


import fr.univlorraine.pierreludmannchessmate.model.Score;
import fr.univlorraine.pierreludmannchessmate.model.Utilisateur;
import fr.univlorraine.pierreludmannchessmate.repository.ScoreRepository;
import fr.univlorraine.pierreludmannchessmate.repository.UtilisateurRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final UtilisateurRepository utilisateurRepository;
    private final ScoreRepository scoreRepository;

    public HomeController(UtilisateurRepository utilisateurRepository, ScoreRepository scoreRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.scoreRepository = scoreRepository;
    }

    /**
     * Redirection automatique de la racine vers /home.
     */
    @GetMapping("/")
    public String racine() {
        return "redirect:/home";
    }

    /**
     * Affiche la page d'accueil (Dashboard).
     * C'est ici que l'utilisateur choisit entre "Bac à sable" et "JeuPuzzle".
     */
    @GetMapping("/home")
    public String accueil(Model model, Authentication auth) {
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