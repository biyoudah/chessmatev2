package fr.univlorraine.pierreludmannchessmate.controller;

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

/**
 * Contrôleur de gestion de l'authentification.
 * Cette classe gère les fonctionnalités liées à l'authentification des utilisateurs,
 * notamment l'affichage des formulaires de connexion et d'inscription,
 * ainsi que le traitement des demandes d'inscription.
 */
@Controller
public class AuthController {


    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructeur avec injection des dépendances nécessaires.
     * 
     * @param utilisateurRepository Repository pour accéder aux données des utilisateurs
     * @param passwordEncoder Encodeur pour le hachage des mots de passe
     */
    public AuthController(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }


    //Gestion de l'authentification

    /**
     * Affiche le formulaire de connexion.
     * 
     * @return Le nom de la vue à afficher
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * Affiche le formulaire d'inscription.
     * Initialise un nouvel objet DTO pour le formulaire.
     * 
     * @param model Le modèle pour la vue
     * @return Le nom de la vue à afficher
     */
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("utilisateur", new InscriptionUtilisateurDTO());
        return "register";
    }

    /**
     * Traite la soumission du formulaire d'inscription.
     * Vérifie la validité des données, l'unicité de l'email,
     * puis crée un nouvel utilisateur avec les informations fournies.
     * 
     * @param registrationDto DTO contenant les informations d'inscription
     * @param bindingResult Résultat de la validation des données
     * @param model Le modèle pour la vue
     * @return Redirection vers la page de connexion en cas de succès, ou retour au formulaire en cas d'erreur
     */
    @PostMapping("/register")
    public String processRegistration(@Valid @ModelAttribute("utilisateur") InscriptionUtilisateurDTO registrationDto,
                                      BindingResult bindingResult,
                                      Model model) {

        //Vérification si l'e-mail est déjà utilisé (Règle métier côté serveur)
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

        //Vérification des erreurs de validation JSR 380 OU de l'unicité ci-dessus
        if (bindingResult.hasErrors()) {
            return "register";
        }

        //Enregistrement nouvel entité utilisateur
        Utilisateur nouvelUtilisateur = new Utilisateur();
        nouvelUtilisateur.setEmail(registrationDto.getEmail());
        nouvelUtilisateur.setPseudo(registrationDto.getPseudo());

        //Hachage mdp
        nouvelUtilisateur.setPassword(passwordEncoder.encode(registrationDto.getPassword()));

        //Définition du rôle
        nouvelUtilisateur.setRole("USER");

        //Sauvegarde
        utilisateurRepository.save(nouvelUtilisateur);

        //Redirection vers le login avec un message de succès
        return "redirect:/login?success";
    }

}
