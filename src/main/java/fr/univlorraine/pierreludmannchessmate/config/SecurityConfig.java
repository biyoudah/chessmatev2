package fr.univlorraine.pierreludmannchessmate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((requests) -> requests
                        // 1. Ressources statiques (CSS, JS)
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()

                        // 2. Auth (Login/Register)
                        .requestMatchers("/login", "/register").permitAll()

                        // 3. PAGES DU JEU (PUBLIC) : On autorise tout le monde ici
                        .requestMatchers("/", "/home", "/new", "/create", "/show", "/puzzle").permitAll()

                        // 4. ACTIONS DU JEU (PUBLIC) : API et Mouvements
                        .requestMatchers("/api/puzzle", "/move", "/place", "/remove", "/reset").permitAll()

                        // (Optionnel) Tout le reste nécessite une connexion
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/home", true)
                        .permitAll()
                )
                .logout((logout) -> logout.permitAll());

        // Note: Si vous aviez des erreurs 403 sur les POST sans être logué,
        // il faudrait peut-être désactiver CSRF temporairement pour le dev,
        // mais avec Thymeleaf forms, ça devrait aller.

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}