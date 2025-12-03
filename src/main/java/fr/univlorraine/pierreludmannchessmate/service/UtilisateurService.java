package fr.univlorraine.pierreludmannchessmate.service;

// Vous devez créer cette classe d'exception (ou utiliser une existante si vous en avez)
import fr.univlorraine.pierreludmannchessmate.exception.UserAlreadyExistsException;
import fr.univlorraine.pierreludmannchessmate.model.Utilisateur;
import fr.univlorraine.pierreludmannchessmate.repository.UtilisateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder; // Supposé injecté

    public UtilisateurService(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

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