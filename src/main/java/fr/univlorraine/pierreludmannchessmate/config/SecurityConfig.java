package fr.univlorraine.pierreludmannchessmate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration // Pour configurer l'application
@EnableWebSecurity // Active le module de sécurité web de Spring
public class SecurityConfig {

    // Définition des règles de circulation HTTP
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((requests) -> requests // Autorisation d'accès
                        .requestMatchers("/login", "/register", "/css/**", "/js/**").permitAll() // Pages publiques sans connexion
                        .anyRequest().authenticated() // Tout le reste nécessite une connexion
                )
                .formLogin((form) -> form
                        .loginPage("/login") // URL de la page de login
                        .permitAll() // Accès pour tout le monde
                )
                .logout((logout) -> logout.permitAll());

        return http.build();
    }

    // Définition de l'algorithme de chiffrement
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Pour chiffrer les mots de passe ("Hachage")
    }
}