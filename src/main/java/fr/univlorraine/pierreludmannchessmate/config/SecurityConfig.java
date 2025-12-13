package fr.univlorraine.pierreludmannchessmate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de la sécurité de l'application.
 * Cette classe définit les règles d'accès aux différentes ressources de l'application,
 * configure l'authentification par formulaire et gère la déconnexion.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {


    /**
     * Configure la chaîne de filtres de sécurité HTTP.
     * Définit les règles d'accès aux ressources, les pages publiques et protégées,
     * ainsi que la configuration du formulaire de connexion et de la déconnexion.
     * 
     * @param http L'objet HttpSecurity à configurer
     * @return La chaîne de filtres de sécurité configurée
     * @throws Exception Si une erreur survient lors de la configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/webjars/**", "/favicon.ico").permitAll()

                        .requestMatchers("/login", "/register").permitAll()

                        .requestMatchers("/", "/home", "/new", "/create", "/show", "/puzzle").permitAll()

                        .requestMatchers("/api/puzzle", "/move", "/place", "/remove", "/reset", "/changeMode", "/customConfig").permitAll()

                        // 5. Le reste nécessite une connexion
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/home", true) // "true" force la redirection vers home après login
                        .permitAll()
                )
                .logout((logout) -> logout
                        .logoutSuccessUrl("/") // Redirige vers la racine après logout
                        .permitAll()
                );

        return http.build();
    }


    /**
     * Fournit un encodeur de mot de passe pour l'application.
     * Utilise l'algorithme BCrypt pour le hachage sécurisé des mots de passe.
     * 
     * @return Un encodeur de mot de passe BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
