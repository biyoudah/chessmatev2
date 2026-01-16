package fr.univlorraine.pierreludmannchessmate.controller;

import fr.univlorraine.pierreludmannchessmate.DTO.InscriptionUtilisateurDTO;
import fr.univlorraine.pierreludmannchessmate.model.Utilisateur;
import fr.univlorraine.pierreludmannchessmate.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private AuthController controller;

    @Test
    void afficherFormulaireConnexion_ReturnsLoginView() {
        String result = controller.login();
        assert result.equals("login");
    }

    @Test
    void afficherFormulaireInscription_InitializesDTOAndReturnsRegisterView() {
        String result = controller.register(model);

        assert result.equals("register");
        verify(model).addAttribute(eq("utilisateur"), any(InscriptionUtilisateurDTO.class));
    }

    @Test
    void traiterInscription_EmailDejaUtilise_RetourneFormulaire() {
        // Arrange
        InscriptionUtilisateurDTO dto = new InscriptionUtilisateurDTO();
        dto.setEmail("existing@test.com");
        dto.setPseudo("TestUser");
        dto.setPassword("Password123");

        when(utilisateurRepository.findByEmail("existing@test.com"))
                .thenReturn(Optional.of(new Utilisateur()));
        when(bindingResult.hasErrors()).thenReturn(true);

        // Act
        String result = controller.processRegistration(dto, bindingResult, model);

        // Assert
        assert result.equals("register");
        verify(bindingResult).addError(any());
    }

    @Test
    void traiterInscription_ValidationError_RetourneFormulaire() {
        // Arrange
        InscriptionUtilisateurDTO dto = new InscriptionUtilisateurDTO();
        dto.setEmail("new@test.com");
        dto.setPseudo("TestUser");
        dto.setPassword("");  // Password vide

        when(utilisateurRepository.findByEmail("new@test.com"))
                .thenReturn(Optional.empty());
        when(bindingResult.hasErrors()).thenReturn(true);

        // Act
        String result = controller.processRegistration(dto, bindingResult, model);

        // Assert
        assert result.equals("register");
    }

    @Test
    void traiterInscription_SuccessfulRegistration_RedirectsToLogin() {
        // Arrange
        InscriptionUtilisateurDTO dto = new InscriptionUtilisateurDTO();
        dto.setEmail("newuser@test.com");
        dto.setPseudo("NewUser");
        dto.setPassword("Password123");

        when(utilisateurRepository.findByEmail("newuser@test.com"))
                .thenReturn(Optional.empty());
        when(bindingResult.hasErrors()).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("hashedPassword");

        // Act
        String result = controller.processRegistration(dto, bindingResult, model);

        // Assert
        assert result.equals("redirect:/login?success");
        verify(utilisateurRepository).save(argThat(utilisateur ->
                utilisateur.getEmail().equals("newuser@test.com") &&
                        utilisateur.getPseudo().equals("NewUser") &&
                        utilisateur.getPassword().equals("hashedPassword") &&
                        utilisateur.getRole().equals("USER")
        ));
    }

    @Test
    void traiterInscription_PasswordEncoded() {
        // Arrange
        InscriptionUtilisateurDTO dto = new InscriptionUtilisateurDTO();
        dto.setEmail("user@test.com");
        dto.setPseudo("User");
        dto.setPassword("RawPassword");

        when(utilisateurRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.empty());
        when(bindingResult.hasErrors()).thenReturn(false);
        when(passwordEncoder.encode("RawPassword")).thenReturn("encryptedPassword");

        // Act
        controller.processRegistration(dto, bindingResult, model);

        // Assert
        verify(passwordEncoder).encode("RawPassword");
        verify(utilisateurRepository).save(argThat(utilisateur ->
                utilisateur.getPassword().equals("encryptedPassword")
        ));
    }

    @Test
    void traiterInscription_SetRoleToUSER() {
        // Arrange
        InscriptionUtilisateurDTO dto = new InscriptionUtilisateurDTO();
        dto.setEmail("user@test.com");
        dto.setPseudo("User");
        dto.setPassword("Password");

        when(utilisateurRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.empty());
        when(bindingResult.hasErrors()).thenReturn(false);
        when(passwordEncoder.encode("Password")).thenReturn("hashed");

        // Act
        controller.processRegistration(dto, bindingResult, model);

        // Assert
        verify(utilisateurRepository).save(argThat(utilisateur ->
                utilisateur.getRole().equals("USER")
        ));
    }
}
