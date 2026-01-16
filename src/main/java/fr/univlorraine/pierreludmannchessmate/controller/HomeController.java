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

/**
 * Contrôleur de la page d'accueil (Dashboard).
 * <p>
 * Expose les routes racine et "/home" permettant d'afficher le tableau de bord
 * avec les informations publiques (articles récents, tournois en direct) et
 * les informations de session (pseudo, statut de connexion).
 */
@Controller
public class HomeController {

    private final UtilisateurRepository utilisateurRepository;
    private final ScoreRepository scoreRepository;
    private final ChessApiService chessApiService; // Ajout du service

    /**
     * Constructeur avec injection des dépendances nécessaires.
     *
     * @param utilisateurRepository accès aux utilisateurs (récupération du pseudo)
     * @param scoreRepository accès aux scores (réutilisable dans le dashboard)
     * @param chessApiService service pour récupérer des contenus récents (API externe)
     */
    public HomeController(UtilisateurRepository utilisateurRepository,
                          ScoreRepository scoreRepository,
                          ChessApiService chessApiService) {
        this.utilisateurRepository = utilisateurRepository;
        this.scoreRepository = scoreRepository;
        this.chessApiService = chessApiService;
    }

    /**
     * Redirige la racine de l'application vers la page d'accueil.
     *
     * @return une redirection HTTP vers la route "/home"
     */
    @GetMapping("/")
    public String racine() {
        return "redirect:/home";
    }

    /**
     * Affiche la page d'accueil (dashboard) avec le contexte utilisateur et
     * des informations externes (articles, tournois en direct).
     *
     * @param model modèle passé à la vue Thymeleaf
     * @param auth informations d'authentification de l'utilisateur courant
     * @return le nom de la vue Thymeleaf à rendre
     */
    @GetMapping("/home")
    public String accueil(Model model, Authentication auth) {
        injecterInfosUtilisateur(model, auth);

        model.addAttribute("articles", chessApiService.getRecentArticles());
        model.addAttribute("tournaments", chessApiService.getLiveTournaments()); // <--- IMPORTANT

        return "home";
    }

    /**
     * Injecte dans le modèle des informations de session: statut de connexion
     * et pseudo de l'utilisateur si disponible.
     *
     * @param model modèle de la vue
     * @param authentication objet d'authentification Spring Security
     */
    private void injecterInfosUtilisateur(Model model, Authentication authentication) {
        boolean isConnected = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);

        model.addAttribute("isLoggedIn", isConnected);

        if (isConnected) {
            String emailUtilisateur = authentication.getName();

            String pseudo = utilisateurRepository.findByEmail(emailUtilisateur)
                    .map(Utilisateur::getPseudo) // On extrait juste le pseudo
                    .orElse("Joueur");           // Valeur par défaut si non trouvé

            model.addAttribute("pseudo", pseudo);
        } else {
            model.addAttribute("pseudo", "Invité");
        }
    }
}