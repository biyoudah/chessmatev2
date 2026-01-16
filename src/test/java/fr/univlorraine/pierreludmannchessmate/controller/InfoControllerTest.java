package fr.univlorraine.pierreludmannchessmate.controller;

import fr.univlorraine.pierreludmannchessmate.model.Utilisateur;
import fr.univlorraine.pierreludmannchessmate.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.Model;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InfoControllerTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private InfoController controller;

    @Test
    void infos_PreparesModelAndReturnsInfosView() {
        when(authentication.isAuthenticated()).thenReturn(true);

        String result = controller.infos(model, authentication);

        assert result.equals("infos");
        verify(model).addAttribute(eq("isLoggedIn"), any());
    }

    @Test
    void infos_UtilisateurConnecte_InjectsPseudo() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user@test.com");
        when(utilisateurRepository.findByEmail(eq("user@test.com")))
                .thenReturn(Optional.of(mock(Utilisateur.class)));

        // Act
        String result = controller.infos(model, authentication);

        // Assert
        assert result.equals("infos");
        verify(model).addAttribute(eq("isLoggedIn"), eq(true));
    }

    @Test
    void infos_UtilisateurNonConnecte_InjectsGuestStatus() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(false);

        // Act
        String result = controller.infos(model, authentication);

        // Assert
        assert result.equals("infos");
        verify(model).addAttribute(eq("isLoggedIn"), eq(false));
        verify(model).addAttribute(eq("pseudo"), eq("Invité"));
    }

    @Test
    void infos_PseudoNotFound_UsesDefaultValue() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("unknown@test.com");
        when(utilisateurRepository.findByEmail(eq("unknown@test.com")))
                .thenReturn(Optional.empty());

        // Act
        String result = controller.infos(model, authentication);

        // Assert
        assert result.equals("infos");
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
