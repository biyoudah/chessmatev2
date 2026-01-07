package fr.univlorraine.pierreludmannchessmate.controller;


import fr.univlorraine.pierreludmannchessmate.model.Score;
import fr.univlorraine.pierreludmannchessmate.model.Utilisateur;
import fr.univlorraine.pierreludmannchessmate.repository.ScoreRepository;
import fr.univlorraine.pierreludmannchessmate.repository.UtilisateurRepository;
import fr.univlorraine.pierreludmannchessmate.service.ChessApiService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final UtilisateurRepository utilisateurRepository;
    private final ScoreRepository scoreRepository;
    private final ChessApiService chessApiService; // Ajout du service

    public HomeController(UtilisateurRepository utilisateurRepository,
                          ScoreRepository scoreRepository,
                          ChessApiService chessApiService) {
        this.utilisateurRepository = utilisateurRepository;
        this.scoreRepository = scoreRepository;
        this.chessApiService = chessApiService;
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
        injecterInfosUtilisateur(model, auth);

        // Ajoute ces deux lignes :
        model.addAttribute("articles", chessApiService.getRecentArticles());
        model.addAttribute("tournaments", chessApiService.getLiveTournaments()); // <--- IMPORTANT

        return "home";
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