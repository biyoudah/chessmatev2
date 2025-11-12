package fr.univlorraine.pierreludmannchessmate;

import fr.univlorraine.pierreludmannchessmate.model.Utilisateur;
import fr.univlorraine.pierreludmannchessmate.repository.UtilisateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Nécéssaire pour le GET /register
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping; // Nécéssaire pour le POST /register

@Controller
public class AuthController {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Affiche le formulaire de connexion
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // Affiche le formulaire d'inscription (GET)
    // 🔑 Ajout de l'objet 'utilisateur' au modèle pour la liaison Thymeleaf (th:object="${utilisateur}")
    @GetMapping("/register")
    public String register(Model model) {
        // Thymeleaf a besoin de cet objet vide pour construire le formulaire
        model.addAttribute("utilisateur", new Utilisateur());
        return "register";
    }

    // Traite la soumission du formulaire d'inscription (POST)
    @PostMapping("/register")
    public String processRegistration(Utilisateur utilisateur, Model model) {

        // 1. Vérification si le pseudo existe déjà
        if (utilisateurRepository.findByEmail(utilisateur.getEmail()).isPresent()) {
            // Rajoute l'objet 'utilisateur' et l'erreur pour que l'utilisateur puisse corriger le formulaire
            model.addAttribute("utilisateur", utilisateur);
            model.addAttribute("error", "Ce mail est déjà pris !");
            return "register";
        }

        // 2. Chiffrer le mot de passe
        String hashedPassword = passwordEncoder.encode(utilisateur.getPassword());
        utilisateur.setPassword(hashedPassword);

        // 3. Définir le rôle par défaut (nécessaire pour Spring Security)
        utilisateur.setRole("USER");

        // 4. Sauvegarde dans la base de données
        utilisateurRepository.save(utilisateur);

        // 5. Rediriger vers le login avec un message de succès
        return "redirect:/show?registered=true";
    }
}