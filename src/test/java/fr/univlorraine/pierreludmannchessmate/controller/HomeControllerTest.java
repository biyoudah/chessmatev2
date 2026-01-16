package fr.univlorraine.pierreludmannchessmate.controller;

import fr.univlorraine.pierreludmannchessmate.model.Utilisateur;
import fr.univlorraine.pierreludmannchessmate.repository.ScoreRepository;
import fr.univlorraine.pierreludmannchessmate.repository.UtilisateurRepository;
import fr.univlorraine.pierreludmannchessmate.service.ChessApiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private ScoreRepository scoreRepository;

    @Mock
    private ChessApiService chessApiService;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private HomeController controller;

    @Test
    void racine_RedirectsToHome() {
        String result = controller.racine();
        assert result.equals("redirect:/home");
    }

    @Test
    void afficherAccueil_PreparesModelWithArticlesAndTournaments() {
        List<Map<String, Object>> mockArticles = List.of();
        List<Map<String, Object>> mockTournaments = List.of();
        when(chessApiService.getRecentArticles()).thenReturn(mockArticles);
        when(chessApiService.getLiveTournaments()).thenReturn(mockTournaments);

        String result = controller.accueil(model, authentication);

        assert result.equals("home");
        verify(model).addAttribute("articles", mockArticles);
        verify(model).addAttribute("tournaments", mockTournaments);
    }

    @Test
    void accueil_UtilisateurConnecte_InjectsPseudo() {
        List<Map<String, Object>> mockArticles = List.of();
        List<Map<String, Object>> mockTournaments = List.of();
        when(chessApiService.getRecentArticles()).thenReturn(mockArticles);
        when(chessApiService.getLiveTournaments()).thenReturn(mockTournaments);

        Utilisateur user = new Utilisateur();

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user@test.com");
        when(utilisateurRepository.findByEmail(eq("user@test.com")))
                .thenReturn(Optional.of(user));
        String result = controller.accueil(model, authentication);
        assert result.equals("home");
        verify(model).addAttribute(eq("isLoggedIn"), eq(true));
        verify(model).addAttribute(eq("pseudo"), any());
    }

        @Test
    void afficherAccueil_UtilisateurNonConnecte_InjectsGuestStatus() {
        List<Map<String, Object>> mockArticles = List.of();
        List<Map<String, Object>> mockTournaments = List.of();
        when(chessApiService.getRecentArticles()).thenReturn(mockArticles);
        when(chessApiService.getLiveTournaments()).thenReturn(mockTournaments);
        when(authentication.isAuthenticated()).thenReturn(false);

        String result = controller.accueil(model, authentication);

        assert result.equals("home");
        verify(model).addAttribute(eq("isLoggedIn"), eq(false));
        verify(model).addAttribute(eq("pseudo"), eq("Invité"));
    }

    @Test
    void afficherAccueil_PseudoNotFound_UsesDefaultValue() {
        List<Map<String, Object>> mockArticles = List.of();
        List<Map<String, Object>> mockTournaments = List.of();
        when(chessApiService.getRecentArticles()).thenReturn(mockArticles);
        when(chessApiService.getLiveTournaments()).thenReturn(mockTournaments);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("unknown@test.com");
        when(utilisateurRepository.findByEmail(eq("unknown@test.com")))
                .thenReturn(Optional.empty());  // Pseudo non trouvé

        String result = controller.accueil(model, authentication);

        verify(model).addAttribute(eq("pseudo"), eq("Joueur"));
    }
    @Test
    void injecterInfosUtilisateur_UtilisateurConnecte() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user@test.com");
        when(utilisateurRepository.findByEmail(eq("user@test.com")))
                .thenReturn(Optional.of(mock(Utilisateur.class)));

        ReflectionTestUtils.invokeMethod(controller, "injecterInfosUtilisateur", model, authentication);

        verify(model).addAttribute(eq("isLoggedIn"), eq(true));
    }

    @Test
    void injecterInfosUtilisateur_UtilisateurNonConnecte() {
        when(authentication.isAuthenticated()).thenReturn(false);

        ReflectionTestUtils.invokeMethod(controller, "injecterInfosUtilisateur", model, authentication);

        verify(model).addAttribute(eq("isLoggedIn"), eq(false));
        verify(model).addAttribute(eq("pseudo"), eq("Invité"));
    }
}
