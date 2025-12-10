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
                        // 1. Ressources statiques (CSS, JS, images, webjars)
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/webjars/**").permitAll()

                        // 2. Auth (Login/Register)
                        .requestMatchers("/login", "/register").permitAll()

                        // 3. Pages du jeu (publiques)
                        .requestMatchers("/", "/home", "/new", "/create", "/show", "/puzzle").permitAll()

                        // 4. Actions du jeu (publiques)
                        .requestMatchers("/api/puzzle", "/move", "/place", "/remove", "/reset").permitAll()

                        // 5. Le reste nécessite une connexion
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/home", false)
                        .permitAll()
                )
                .logout((logout) -> logout.permitAll());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
