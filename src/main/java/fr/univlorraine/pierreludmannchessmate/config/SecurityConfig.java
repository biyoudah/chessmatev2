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
 * Définit les accès aux nouveaux contrôleurs de placement et de puzzle.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // --- AJOUT CRUCIAL ICI ---
                // On désactive la protection CSRF uniquement pour les URLs du puzzle.
                // Cela permet aux requêtes POST (move, computer-move) envoyées par JavaScript (fetch)
                // de passer sans être bloquées ou redirigées vers le login.
                .csrf(csrf -> csrf.ignoringRequestMatchers("/puzzle/**", "/placement/**"))

                .authorizeHttpRequests((requests) -> requests
                        // 1. Ressources statiques (toujours publiques)
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/webjars/**", "/favicon.ico").permitAll()

                        // 2. Authentification (publique)
                        .requestMatchers("/login", "/register").permitAll()

                        // 3. Navigation principale (publique)
                        .requestMatchers("/", "/home").permitAll()

                        // 4. Nouveau Mode Placement (Bac à sable)
                        .requestMatchers("/placement/**").permitAll()

                        // 5. Nouveau Mode Puzzle
                        // Autorise l'accès à la page et aux actions associées
                        .requestMatchers("/puzzle/**").permitAll()

                        // 6. Le reste nécessite une connexion
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/home", true)
                        .permitAll()
                )
                .logout((logout) -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}