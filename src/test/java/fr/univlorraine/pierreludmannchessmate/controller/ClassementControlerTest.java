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
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ClassementControllerTest {

    @Mock
    private ScoreRepository scoreRepository;

    @Mock
    private Model model;

    @InjectMocks
    private ClassementController controller;

    @Test
    void afficherClassement_NoMode_NoSchema_GetClassementGlobal() {
        List<ClassementRow> mockClassement = List.of();
        when(scoreRepository.getClassementGlobal()).thenReturn(mockClassement);

        String result = controller.afficherClassement(null, null, model);

        assertEquals("classement", result);
        verify(scoreRepository).getClassementGlobal();
        verify(model).addAttribute("classement", mockClassement);
        verify(model).addAttribute("modeSelectionne", null);
        verify(model).addAttribute("schemaKeySelectionne", null);
    }

    @Test
    void afficherClassement_WithMode_NoSchema_GetClassementParMode() {
        List<ClassementRow> mockClassement = List.of();
        String mode = "PLACEMENT";
        when(scoreRepository.getClassementParMode(mode)).thenReturn(mockClassement);

        String result = controller.afficherClassement(mode, null, model);

        assertEquals("classement", result);
        verify(scoreRepository).getClassementParMode(mode);
        verify(model).addAttribute("modeSelectionne", mode);
        verify(model).addAttribute("schemaKeySelectionne", null);
    }

    @Test
    void afficherClassement_NoMode_WithSchema_GetClassementParSchemaKey() {
        List<ClassementRow> mockClassement = List.of();
        String schema = "8-dames|{Dame=8}";
        when(scoreRepository.getClassementParSchemaKey(schema)).thenReturn(mockClassement);

        String result = controller.afficherClassement(null, schema, model);

        assertEquals("classement", result);
        verify(scoreRepository).getClassementParSchemaKey(schema);
        verify(model).addAttribute("schemaKeySelectionne", schema);
    }

    @Test
    void afficherClassement_PrioritySchemaOverMode() {
        // Si les deux sont fournis, schemaKey doit avoir la priorité
        List<ClassementRow> mockClassement = List.of();
        String mode = "PLACEMENT";
        String schema = "custom";
        when(scoreRepository.getClassementParSchemaKey(schema)).thenReturn(mockClassement);

        String result = controller.afficherClassement(mode, schema, model);

        assertEquals("classement", result);
        // On vérifie que c'est bien la méthode par SchemaKey qui est appelée
        verify(scoreRepository).getClassementParSchemaKey(schema);
        verify(scoreRepository, never()).getClassementParMode(anyString());
    }

    @Test
    void afficherClassement_ModeToutes_GetClassementGlobal() {
        List<ClassementRow> mockClassement = List.of();
        when(scoreRepository.getClassementGlobal()).thenReturn(mockClassement);

        String result = controller.afficherClassement("TOUS", null, model);

        assertEquals("classement", result);
        verify(scoreRepository).getClassementGlobal();
    }

    @Test
    void afficherClassement_SchemaTOUT_GetClassementGlobal() {
        // Test du cas où schemaKey est "TOUT" (devrait revenir au global ou au mode)
        List<ClassementRow> mockClassement = List.of();
        when(scoreRepository.getClassementGlobal()).thenReturn(mockClassement);

        String result = controller.afficherClassement(null, "TOUT", model);

        assertEquals("classement", result);
        verify(scoreRepository).getClassementGlobal();
    }
}