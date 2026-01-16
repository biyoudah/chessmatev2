package fr.univlorraine.pierreludmannchessmate.controller;

import fr.univlorraine.pierreludmannchessmate.logic.JeuPuzzle;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.Model;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PuzzleControllerTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private ScoreRepository scoreRepository;

    @Mock
    private JeuPuzzle jeuPuzzle;

    @Mock
    private HttpSession session;

    @Mock
    private Authentication authentication;

    @Mock
    private Model model;

    @InjectMocks
    private PuzzleController controller;

    @Test
    void initPuzzle_ReturnsNewJeuPuzzle() {
        JeuPuzzle result = controller.initPuzzle();
        assert result != null;
    }

    @Test
    void afficherPuzzle_PreparesModelWithBoard() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(jeuPuzzle.getBoard()).thenReturn(new String[8][8]);

        controller.afficherPuzzle(jeuPuzzle, model, authentication, session);

        verify(model).addAttribute(eq("isLoggedIn"), any());
        verify(model).addAttribute(eq("board"), any());
        verify(model).addAttribute(eq("rows"), any());
        verify(model).addAttribute(eq("cols"), any());
    }

    @Test
    void getHint_SetsCoordsInSession() {
        when(jeuPuzzle.getCoupAide()).thenReturn("e4");

        ResponseEntity<?> result = controller.getHint(jeuPuzzle, session);

        assert result.getStatusCode().is2xxSuccessful();
        verify(session).setAttribute("hintCoords", "e4");
    }

    @Test
    void handleMove_UnauthorizedUser_Returns401() {
        when(authentication.isAuthenticated()).thenReturn(false);

        ResponseEntity<?> result = controller.handleMove(0, 0, 1, 1, jeuPuzzle, session, authentication);

        assert result.getStatusCode().value() == 401;
    }

    @Test
    void handleMove_SuccessfulMove_SetFlashVictory() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(jeuPuzzle.jouerCoupJoueur(anyInt(), anyInt(), anyInt(), anyInt())).thenReturn("GAGNE");

        ResponseEntity<?> result = controller.handleMove(4, 1, 4, 3, jeuPuzzle, session, authentication);

        assert result.getStatusCode().is2xxSuccessful();
        verify(session).setAttribute("flashMessage", "Puzzle Résolu !");
        verify(session).setAttribute("flashType", "victory");
    }

    @Test
    void handleMove_InvalidMove_SetFlashError() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(jeuPuzzle.jouerCoupJoueur(anyInt(), anyInt(), anyInt(), anyInt())).thenReturn("ECHEC");

        ResponseEntity<?> result = controller.handleMove(0, 0, 0, 1, jeuPuzzle, session, authentication);

        assert result.getStatusCode().is2xxSuccessful();
        verify(session).setAttribute("flashType", "error");
    }

    @Test
    void computerMove_ExecutesMove() {
        ResponseEntity<?> result = controller.computerMove(jeuPuzzle);

        assert result.getStatusCode().is2xxSuccessful();
        verify(jeuPuzzle).reponseOrdinateur();
    }

    @Test
    void changeMode_SetsDifficultyAndLoadsNewPuzzle() {
        when(jeuPuzzle.getDifficulte()).thenReturn("2");

        ResponseEntity<?> result = controller.changeMode("2", jeuPuzzle, session);

        assert result.getStatusCode().is2xxSuccessful();
        verify(jeuPuzzle).setDifficulte("2");
    }

    @Test
    void resetPuzzle_LoadsNewPuzzle() {
        when(jeuPuzzle.getDifficulte()).thenReturn("1");

        ResponseEntity<?> result = controller.resetPuzzle(jeuPuzzle, session);

        assert result.getStatusCode().is2xxSuccessful();
        verify(jeuPuzzle).setScoreEnregistre(false);
    }


    @Test
    void clearBoard_EmptiesBoard() {
        ResponseEntity<?> result = controller.clearBoard(jeuPuzzle, session);

        assert result.getStatusCode().is2xxSuccessful();
        verify(jeuPuzzle).viderPlateau();
    }

    @Test
    void traiterVictoirePuzzle_ConnectedUser_SavesScore() {
        when(jeuPuzzle.isScoreEnregistre()).thenReturn(false);
        when(jeuPuzzle.getDifficulte()).thenReturn("1");
        when(jeuPuzzle.getPuzzleId()).thenReturn("puzzle123");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user@test.com");
        Utilisateur user = new Utilisateur();
        when(utilisateurRepository.findByEmail(eq("user@test.com"))).thenReturn(Optional.of(user));
        when(scoreRepository.existsByUtilisateurAndSchemaKey(eq(user), anyString())).thenReturn(false);
        ReflectionTestUtils.invokeMethod(controller, "traiterVictoirePuzzle",
                jeuPuzzle, session, authentication);

        verify(scoreRepository).save(any(Score.class));
    }


    @Test
    void traiterVictoirePuzzle_AlreadyScored_DoesNotSaveAgain() {
        when(jeuPuzzle.isScoreEnregistre()).thenReturn(true);

        ReflectionTestUtils.invokeMethod(controller, "traiterVictoirePuzzle",
                jeuPuzzle, session, authentication);

        verify(scoreRepository, never()).save(any());
    }
}
