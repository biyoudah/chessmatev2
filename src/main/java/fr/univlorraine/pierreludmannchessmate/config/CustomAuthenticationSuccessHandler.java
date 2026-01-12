package fr.univlorraine.pierreludmannchessmate.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // 1. On récupère le paramètre "targetUrl" envoyé par le formulaire
        String targetUrl = request.getParameter("targetUrl");

        // 2. Si l'URL existe et n'est pas vide
        if (targetUrl != null && !targetUrl.isEmpty()) {
            // On redirige vers cette URL spécifique (ex: /placement)
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        } else {
            // 3. Sinon, on redirige vers l'accueil par défaut
            // Note : on utilise setDefaultTargetUrl pour définir le fallback
            setDefaultTargetUrl("/home");
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }
}