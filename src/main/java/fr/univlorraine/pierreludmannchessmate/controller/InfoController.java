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
 * Contrôleur de la page d'informations.
 * <p>
 * Fournit la route "/infos" affichant une page statique enrichie du contexte
 * de session (statut de connexion et pseudo si disponible).
 */
@Controller
public class InfoController {

    private final UtilisateurRepository utilisateurRepository;

    /**
     * Constructeur avec injection du dépôt utilisateur.
     *
     * @param utilisateurRepository accès aux utilisateurs pour récupérer le pseudo
     */
    public InfoController(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    /**
     * Affiche la page d'informations.
     *
     * @param model modèle pour la vue Thymeleaf
     * @param auth informations d'authentification de l'utilisateur courant
     * @return le nom de la vue à rendre
     */
    @GetMapping("/infos")
    public String infos(Model model, Authentication auth) {
        injecterInfosUtilisateur(model, auth);
        return "infos";
    }

    /**
     * Injecte dans le modèle le statut de connexion et le pseudo utilisateur.
     *
     * @param model modèle de la vue
     * @param authentication objet Spring Security décrivant l'utilisateur courant
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


