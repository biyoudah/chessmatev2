package fr.univlorraine.pierreludmannchessmate.controller;

import fr.univlorraine.pierreludmannchessmate.repository.ScoreRepository;
import fr.univlorraine.pierreludmannchessmate.repository.ScoreRepository.ClassementRow;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ClassementController {

    private final ScoreRepository scoreRepository;


    public ClassementController(ScoreRepository scoreRepository) {
        this.scoreRepository = scoreRepository;
    }

    @GetMapping("/classement")
    public String afficherClassement(@RequestParam(required = false) String mode, Model model) {
        List<ClassementRow> classement;

        if (mode != null && !mode.isEmpty() && !mode.equals("TOUS")) {
            classement = scoreRepository.getClassementParMode(mode);
        } else {
            classement = scoreRepository.getClassementGlobal();
        }

        model.addAttribute("classement", classement);
        model.addAttribute("modeSelectionne", mode); // Pour garder la sélection dans la liste déroulante

        return "classement"; // Renvoie vers le fichier classement.html
    }
}
