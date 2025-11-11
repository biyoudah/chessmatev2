package fr.univlorraine.pierreludmannchessmate;

import fr.univlorraine.pierreludmannchessmate.repository.UtilisateurRepository;
import org.springframework.stereotype.Controller;
import fr.univlorraine.pierreludmannchessmate.model.Utilisateur;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {
    private final UtilisateurRepository utilisateur;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UtilisateurRepository u, PasswordEncoder p) {
        this.utilisateur = u;
        this.passwordEncoder = p;
    }

    // Affiche le formulaire de connexion
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // Affiche le formulaire d'inscription
    @GetMapping("/register")
    public String register() {
        return "register";
    }


}
