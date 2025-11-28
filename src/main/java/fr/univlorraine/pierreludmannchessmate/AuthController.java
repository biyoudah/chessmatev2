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
import org.springframework.web.bind.annotation.PostMapping;

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
        // Ajout de l'objet DTO pour la liaison du formulaire Thymeleaf
        model.addAttribute("utilisateur", new InscriptionUtilisateurDTO());
        return "register";
    }

    // Traite la soumission du formulaire d'inscription (POST)
    @PostMapping("/register")
    public String processRegistration(@Valid InscriptionUtilisateurDTO registrationDto,
                                      BindingResult bindingResult,
                                      Model model) {

        // 1. Vérification des erreurs de validation JSR 380
        if (bindingResult.hasErrors()) {
            // Repasse les données et retourne À LA VUE (sans redirection)
            // Cela permet à Thymeleaf d'utiliser le BindingResult pour th:errors et th:classappend
            model.addAttribute("utilisateur", registrationDto);
            // La ligne System.out.println("erreur d'une erreur"); est inutile et peut être retirée.
            return "register"; // <-- CORRECTION : Retirer le "?error"
        }


        // 2. Vérification si l'e-mail est déjà utilisé
        if (utilisateurRepository.findByEmail(registrationDto.getEmail()).isPresent()) {
            // Si l'utilisateur existe, ajouter une erreur générale au modèle
            model.addAttribute("error", "Cet email est déjà associé à un compte.");
            // Repasse l'objet DTO pour garder les autres données saisies
            model.addAttribute("utilisateur", registrationDto);
            return "register"; // Retourne à la page d'inscription pour afficher l'erreur générale
        }


        // 3. Création de l'objet Utilisateur à partir du DTO
        Utilisateur nouvelUtilisateur = new Utilisateur();
        nouvelUtilisateur.setEmail(registrationDto.getEmail());
        nouvelUtilisateur.setPseudo(registrationDto.getPseudo());

        // 4. Chiffrement du mot de passe
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