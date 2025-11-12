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

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    public CustomUserDetailsService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    // Cette méthode est appelée par Spring Security lors d'une tentative de connexion
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