package fr.univlorraine.pierreludmannchessmate.controller;

import fr.univlorraine.pierreludmannchessmate.repository.ScoreRepository;
import fr.univlorraine.pierreludmannchessmate.repository.ScoreRepository.ClassementRow;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Contrôleur de consultation des classements.
 * <p>
 * Fournit une page récapitulant les meilleurs scores globalement ou par mode
 * de jeu, en s'appuyant sur des projections renvoyées par {@link ScoreRepository}.
 */
@Controller
public class ClassementController {

    private final ScoreRepository scoreRepository;


    /**
     * Injection du dépôt des scores.
     * @param scoreRepository repository pour l'accès aux classements
     */
    public ClassementController(ScoreRepository scoreRepository) {
        this.scoreRepository = scoreRepository;
    }

    /**
     * Affiche le classement.
     *
     * @param mode filtre optionnel (ex: PUZZLE, PLACEMENT) ou {@code null}/TOUS pour le global
     * @param model modèle de la vue
     * @return le nom de la vue Thymeleaf à rendre
     */
    @GetMapping("/classement")
    public String afficherClassement(@RequestParam(required = false) String mode, Model model) {
        List<ClassementRow> classement;

        if (mode != null && !mode.isEmpty() && !mode.equals("TOUS")) {
            classement = scoreRepository.getClassementParMode(mode);
        } else {
            classement = scoreRepository.getClassementGlobal();
        }

        model.addAttribute("classement", classement);
        model.addAttribute("modeSelectionne", mode);

        return "classement";
    }
}
