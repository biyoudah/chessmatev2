package fr.univlorraine.pierreludmannchessmate.controller;

import fr.univlorraine.pierreludmannchessmate.repository.ScoreRepository;
import fr.univlorraine.pierreludmannchessmate.repository.ScoreRepository.ClassementRow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassementControllerTest {

    @Mock
    private ScoreRepository scoreRepository;

    @Mock
    private Model model;

    @InjectMocks
    private ClassementController controller;

    @Test
    void afficherClassement_NoMode_GetClassementGlobal() {
        List<ClassementRow> mockClassement = List.of();
        when(scoreRepository.getClassementGlobal()).thenReturn(mockClassement);
        String result = controller.afficherClassement(null, model);
        assert result.equals("classement");
        verify(scoreRepository).getClassementGlobal();
        verify(model).addAttribute("classement", mockClassement);
        verify(model).addAttribute("modeSelectionne", null);
    }

    @Test
    void afficherClassement_WithMode_GetClassementParMode() {
        List<ClassementRow> mockClassement = List.of();
        when(scoreRepository.getClassementParMode("8-dames")).thenReturn(mockClassement);
        String result = controller.afficherClassement("8-dames", model);
        assert result.equals("classement");
        verify(scoreRepository).getClassementParMode("8-dames");
        verify(model).addAttribute("classement", mockClassement);
        verify(model).addAttribute("modeSelectionne", "8-dames");
    }

    @Test
    void afficherClassement_ModeToutes_GetClassementGlobal() {
        List<ClassementRow> mockClassement = List.of();
        when(scoreRepository.getClassementGlobal()).thenReturn(mockClassement);
        String result = controller.afficherClassement("TOUS", model);
        assert result.equals("classement");
        verify(scoreRepository).getClassementGlobal();
        verify(model).addAttribute("classement", mockClassement);
    }

    @Test
    void afficherClassement_ModeEmpty_GetClassementGlobal() {
        List<ClassementRow> mockClassement = List.of();
        when(scoreRepository.getClassementGlobal()).thenReturn(mockClassement);
        String result = controller.afficherClassement("", model);
        assert result.equals("classement");
        verify(scoreRepository).getClassementGlobal();
    }
}
