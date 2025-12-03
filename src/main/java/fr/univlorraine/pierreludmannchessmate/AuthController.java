package fr.univlorraine.pierreludmannchessmate;

import fr.univlorraine.pierreludmannchessmate.DTO.InscriptionUtilisateurDTO;
import fr.univlorraine.pierreludmannchessmate.model.Utilisateur;
import fr.univlorraine.pierreludmannchessmate.repository.UtilisateurRepository;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.validation.FieldError;

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
    @GetMapping("/register")
    public String register(Model model) {
        if (!model.containsAttribute("utilisateur")) {
            model.addAttribute("utilisateur", new InscriptionUtilisateurDTO());
        }
        return "register";
    }

    // Traite la soumission du formulaire d'inscription (POST)
    @PostMapping("/register")
    public String processRegistration(@Valid @ModelAttribute("utilisateur") InscriptionUtilisateurDTO registrationDto,
                                      BindingResult bindingResult,
                                      Model model) {

        // 1. Vérification si l'e-mail est déjà utilisé (Règle métier côté serveur)
        if (utilisateurRepository.findByEmail(registrationDto.getEmail()).isPresent()) {
            bindingResult.addError(new FieldError(
                    "utilisateur",
                    "email",
                    registrationDto.getEmail(),
                    false,
                    null,
                    null,
                    "Email déjà utilisé, veuillez changer d'email."
            ));
        }

        // 2. Vérification des erreurs de validation JSR 380 OU de l'unicité ci-dessus
        if (bindingResult.hasErrors()) {
            return "register";
        }

        // --- Si tout est valide ---

        // 3. Création et préparation de l'objet Utilisateur
        Utilisateur nouvelUtilisateur = new Utilisateur();
        nouvelUtilisateur.setEmail(registrationDto.getEmail());
        nouvelUtilisateur.setPseudo(registrationDto.getPseudo());

        // 4. Chiffrement et définition du mot de passe
        String hashedPassword = passwordEncoder.encode(registrationDto.getPassword());
        nouvelUtilisateur.setPassword(hashedPassword);

        // 5. Définition du rôle par défaut
        nouvelUtilisateur.setRole("USER");

        // 6. Sauvegarde
        utilisateurRepository.save(nouvelUtilisateur);

        // 7. Redirection vers le login avec un message de succès
        return "redirect:/login?success";
    }

}