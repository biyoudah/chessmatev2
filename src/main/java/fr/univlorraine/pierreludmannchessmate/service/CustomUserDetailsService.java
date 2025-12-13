package fr.univlorraine.pierreludmannchessmate.service;

import fr.univlorraine.pierreludmannchessmate.model.Utilisateur;
import fr.univlorraine.pierreludmannchessmate.repository.UtilisateurRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service personnalisé pour charger les détails des utilisateurs.
 * Cette classe implémente l'interface UserDetailsService de Spring Security
 * pour permettre l'authentification des utilisateurs à partir de la base de données.
 * Elle convertit les entités Utilisateur en objets UserDetails compréhensibles par Spring Security.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    // --- INJECTION DE DÉPENDANCES ---

    private final UtilisateurRepository utilisateurRepository;

    /**
     * Constructeur avec injection du repository des utilisateurs.
     * 
     * @param utilisateurRepository Repository pour accéder aux données des utilisateurs
     */
    public CustomUserDetailsService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    // --- AUTHENTIFICATION DES UTILISATEURS ---

    /**
     * Charge les détails d'un utilisateur à partir de son email.
     * Cette méthode est appelée par Spring Security lors d'une tentative de connexion.
     * Elle recherche l'utilisateur dans la base de données et convertit ses informations
     * en un objet UserDetails utilisable par Spring Security pour l'authentification.
     * 
     * @param email L'email de l'utilisateur (utilisé comme identifiant)
     * @return Les détails de l'utilisateur sous forme d'objet UserDetails
     * @throws UsernameNotFoundException Si aucun utilisateur n'est trouvé avec cet email
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Le paramètre 'email' contient la valeur que l'utilisateur a tapée
        // dans le champ 'username' du formulaire de login (qui est l'email).

        // 1. Chercher l'utilisateur par Email dans la base de données
        Optional<Utilisateur> optionalUser = utilisateurRepository.findByEmail(email);

        Utilisateur utilisateur = optionalUser.orElseThrow(() ->
                new UsernameNotFoundException("Aucun utilisateur trouvé avec l'email : " + email)
        );

        // 2. Construire l'objet UserDetails de Spring Security
        // C'est cet objet que Spring utilisera pour comparer le mot de passe haché
        return User.builder()
                // Le nom d'utilisateur final (ici, on utilise l'email comme identifiant unique)
                .username(utilisateur.getEmail())

                // Le mot de passe haché (stocké dans le champ 'password' de l'entité)
                .password(utilisateur.getPassword())

                // Les autorités (rôles) de l'utilisateur
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + utilisateur.getRole())))
                .build();
    }
}
