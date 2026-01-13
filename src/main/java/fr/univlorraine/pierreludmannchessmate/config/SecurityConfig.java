package fr.univlorraine.pierreludmannchessmate.config;

import org.springframework.beans.factory.annotation.Autowired; // <--- IMPORTER CECI
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

    // 1. ON INJECTE TON HANDLER PERSONNALISÉ ICI
    @Autowired
    private CustomAuthenticationSuccessHandler customSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/puzzle/**", "/placement/**"))

                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/webjars/**", "/favicon.ico").permitAll()
                        .requestMatchers("/login", "/register").permitAll()
                        .requestMatchers("/", "/home").permitAll()
                        .requestMatchers("/placement/**").permitAll()
                        .requestMatchers("/puzzle/**").permitAll()
                        .requestMatchers("/infos/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        // 2. ON REMPLACE .defaultSuccessUrl PAR NOTRE HANDLER
                        // C'est cette ligne qui fait la magie de la redirection dynamique
                        .successHandler(customSuccessHandler)
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