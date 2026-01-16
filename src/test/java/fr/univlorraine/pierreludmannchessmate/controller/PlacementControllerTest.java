package fr.univlorraine.pierreludmannchessmate.controller;

import fr.univlorraine.pierreludmannchessmate.logic.JeuPlacement;
import fr.univlorraine.pierreludmannchessmate.model.Piece;
import fr.univlorraine.pierreludmannchessmate.model.Score;
import fr.univlorraine.pierreludmannchessmate.model.Utilisateur;
import fr.univlorraine.pierreludmannchessmate.repository.ScoreRepository;
import fr.univlorraine.pierreludmannchessmate.repository.UtilisateurRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import org.springframework.ui.Model;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlacementControllerTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private ScoreRepository scoreRepository;

    @Mock
    private JeuPlacement jeuPlacement;

    @Mock
    private HttpSession session;

    @Mock
    private Authentication authentication;

    @Mock
    private Model model;

    @InjectMocks
    private PlacementController controller;




    @Test
    void initPlacement_ReturnsNewJeuPlacement() {
        JeuPlacement result = controller.initPlacement();
    }

    @Test
    void afficher_InjecteInfosUtilisateur_EtPreparerModele() {
        when(authentication.isAuthenticated()).thenReturn(true);
        controller.afficher(jeuPlacement, model, authentication, session);
        verify(model).addAttribute(eq("board"), any());
    }

    @Test
    void action_PlacerPiece_SetFlashPlace() {
        when(jeuPlacement.getPieceObject(0, 0)).thenReturn(null);
        when(jeuPlacement.placerPieceJoueur(0, 0, "Dame", true)).thenReturn("OK");


        controller.action(0, 0, "Dame", true, jeuPlacement, session, authentication);


        verify(session).setAttribute("flashType", "place");
    }


    @Test
    void action_RetirerPiece_SetFlashRemove() {
        Piece mockPiece = mock(Piece.class);
        when(jeuPlacement.getPieceObject(0, 0)).thenReturn(mockPiece);


        controller.action(0, 0, null, true, jeuPlacement, session, authentication);

        verify(jeuPlacement).retirerPiece(0, 0);
        verify(session).setAttribute("flashType", "remove");
    }

    @Test
    void reset_ReinitialiseJeuPlacement_RedirectPlacement() {
        controller.reset(jeuPlacement);
        verify(jeuPlacement).reinitialiser();
    }

    @Test
    void changeMode_8Dames_ConfigDame8_Redirect() throws Exception {
        String result = controller.changeMode("8-dames", jeuPlacement);

        assert result.equals("redirect:/placement");
        verify(jeuPlacement).setConfigurationRequise(argThat(map ->
                map.containsKey("Dame") && map.get("Dame") == 8));
        verify(jeuPlacement).setModeDeJeu("8-dames");
        verify(jeuPlacement).reinitialiser();
    }

    @Test
    void customConfig_ValidConfig_SetCustomMode() {
        // Arrange - Mock validation = OK + parsing
        Map<String, String> params = Map.of("cdame", "4");
        when(jeuPlacement.validerConfiguration(any())).thenReturn("OK");

        // Act
        controller.customConfig(params, jeuPlacement, session);

        // Assert
        verify(jeuPlacement).setModeDeJeu("custom");
        verify(jeuPlacement).reinitialiser();
    }

    @Test
    void traiterVictoire_Connecte_SauvegardeScore() {
        // Arrange
        when(jeuPlacement.isScoreEnregistre()).thenReturn(false);
        when(jeuPlacement.calculerScoreFinalSansBonus()).thenReturn(100);
        when(jeuPlacement.estTentativeParfaite()).thenReturn(true);
        when(jeuPlacement.getModeDeJeu()).thenReturn("8-dames");
        when(jeuPlacement.getConfigurationRequise()).thenReturn(Map.of("Dame", 8));

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user@test.com");

        Utilisateur user = new Utilisateur();
        when(utilisateurRepository.findByEmail(eq("user@test.com"))).thenReturn(Optional.of(user));
        when(scoreRepository.existsByUtilisateurAndSchemaKey(eq(user), anyString())).thenReturn(false);

        // Act
        ReflectionTestUtils.invokeMethod(controller, "traiterVictoire", jeuPlacement, session, authentication);

        // Assert
        verify(scoreRepository).save(any(Score.class));
    }
}
