package fr.univlorraine.pierreludmannchessmate.service;

import fr.univlorraine.pierreludmannchessmate.exception.UserAlreadyExistsException;
import fr.univlorraine.pierreludmannchessmate.model.Utilisateur;
import fr.univlorraine.pierreludmannchessmate.repository.UtilisateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service de gestion des utilisateurs.
 * Cette classe fournit les fonctionnalités nécessaires pour gérer les utilisateurs
 * de l'application, notamment l'enregistrement de nouveaux utilisateurs avec
 * vérification d'unicité et hachage des mots de passe.
 */
@Service
public class UtilisateurService {

    // --- INJECTION DE DÉPENDANCES ---

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructeur avec injection des dépendances nécessaires.
     * 
     * @param utilisateurRepository Repository pour accéder aux données des utilisateurs
     * @param passwordEncoder Encodeur pour le hachage des mots de passe
     */
    public UtilisateurService(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // --- GESTION DES UTILISATEURS ---

    /**
     * Enregistre un nouvel utilisateur dans le système.
     * Cette méthode vérifie d'abord si l'email est déjà utilisé, puis hache le mot de passe
     * avant de sauvegarder l'utilisateur dans la base de données.
     * 
     * @param nouvelUtilisateur L'objet Utilisateur contenant les informations du nouvel utilisateur
     * @return L'utilisateur enregistré avec son ID généré
     * @throws UserAlreadyExistsException Si un utilisateur avec le même email existe déjà
     */
    public Utilisateur enregistrer(Utilisateur nouvelUtilisateur) throws UserAlreadyExistsException {
        // 1. VÉRIFICATION DE L'UNICITÉ
        if (utilisateurRepository.findByEmail(nouvelUtilisateur.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("L'adresse email est déjà utilisée.");
        }

        // 2. Hachage du mot de passe
        nouvelUtilisateur.setPassword(passwordEncoder.encode(nouvelUtilisateur.getPassword()));
        // ... Logique additionnelle (définir le rôle, etc.) ...

        // 3. Sauvegarde
        return utilisateurRepository.save(nouvelUtilisateur);
    }
}
