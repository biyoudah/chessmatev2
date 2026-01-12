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
public class InfoController {

    private final UtilisateurRepository utilisateurRepository;

    public InfoController(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @GetMapping("/infos")
    public String infos(Model model, Authentication auth) {
        injecterInfosUtilisateur(model, auth);
        return "infos";
    }

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


