package fr.univlorraine.pierreludmannchessmate.controller;

import fr.univlorraine.pierreludmannchessmate.JeuPlacement;
import jakarta.servlet.http.HttpSession; // IMPORTANT
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/placement")
@SessionAttributes("jeuPlacement")
public class PlacementController {

    @ModelAttribute("jeuPlacement")
    public JeuPlacement initGame() {
        return new JeuPlacement();
    }

    @GetMapping
    public String showBoard(@ModelAttribute("jeuPlacement") JeuPlacement game,
                            Model model,
                            Authentication auth,
                            HttpSession session) { // AJOUT HttpSession

        // 1. Gestion des messages Flash via Session (pour survivre au fetch)
        Object msg = session.getAttribute("flashMessage");
        if (msg != null) {
            model.addAttribute("message", msg);
            session.removeAttribute("flashMessage");
        }

        injecterInfosUtilisateur(model, auth);
        preparerModele(model, game);
        return "jeuPlacement";
    }

    @PostMapping("/reset")
    public String reset(SessionStatus status, HttpSession session) {
        status.setComplete();
        session.setAttribute("flashMessage", "Plateau réinitialisé.");
        return "redirect:/placement";
    }

    @PostMapping("/action")
    public String action(@RequestParam int x, @RequestParam int y,
                         @RequestParam(required = false) String type,
                         @ModelAttribute("jeuPlacement") JeuPlacement game,
                         HttpSession session) { // Changement ici

        if (game.getPieceObject(x, y) == null) {
            // Placement
            if (type != null && !type.isEmpty()) {
                String resultat = game.placerPieceJoueur(x, y, type, true);
                if (!"OK".equals(resultat)) {
                    session.setAttribute("flashMessage", "⚠️ " + resultat);
                }
            }
        } else {
            // Gomme
            game.retirerPiece(x, y);
        }
        return "redirect:/placement";
    }

    @PostMapping("/changeMode")
    public String changeMode(@RequestParam("modeDeJeu") String mode,
                             @ModelAttribute("jeuPlacement") JeuPlacement game,
                             HttpSession session) {

        Map<String, Integer> config = new HashMap<>();
        switch (mode) {
            case "8-dames" -> config.put("Dame", 8);
            case "8-tours" -> config.put("Tour", 8);
            case "14-fous" -> config.put("Fou", 14);
            case "16-rois" -> config.put("Roi", 16);
            case "custom" -> { /* Attente config custom */ }
            default -> config.put("Dame", 8);
        }

        if (!"custom".equals(mode)) {
            game.setConfigurationRequise(config);
            game.setModeDeJeu(mode);
            game.reinitialiser();
            session.setAttribute("flashMessage", "Mode changé : " + mode);
        } else {
            game.setModeDeJeu("custom");
        }

        return "redirect:/placement";
    }

    @PostMapping("/customConfig")
    public String customConfig(@RequestParam Map<String, String> params,
                               @ModelAttribute("jeuPlacement") JeuPlacement game,
                               HttpSession session) {

        Map<String, Integer> newConfig = new HashMap<>();
        parseConfigParam(params, newConfig, "c_dame", "Dame");
        parseConfigParam(params, newConfig, "c_tour", "Tour");
        parseConfigParam(params, newConfig, "c_fou", "Fou");
        parseConfigParam(params, newConfig, "c_cavalier", "Cavalier");
        parseConfigParam(params, newConfig, "c_roi", "Roi");

        String validation = game.validerConfiguration(newConfig);

        if ("OK".equals(validation)) {
            game.setConfigurationRequise(newConfig);
            game.setModeDeJeu("custom");
            game.reinitialiser();
            session.setAttribute("flashMessage", "✅ Configuration personnalisée appliquée !");
        } else {
            session.setAttribute("flashMessage", "❌ Erreur config : " + validation);
        }

        return "redirect:/placement";
    }

    // --- Utilitaires ---

    private void parseConfigParam(Map<String, String> params, Map<String, Integer> config, String paramName, String pieceType) {
        try {
            String val = params.get(paramName);
            if (val != null && !val.isEmpty()) {
                int nb = Integer.parseInt(val);
                if (nb > 0) config.put(pieceType, nb);
            }
        } catch (NumberFormatException ignored) {}
    }

    private void injecterInfosUtilisateur(Model model, Authentication auth) {
        String pseudo = (auth != null && auth.isAuthenticated()) ? auth.getName() : "Invité";
        model.addAttribute("pseudo", pseudo);
    }

    private void preparerModele(Model model, JeuPlacement game) {
        // IMPORTANT : board est envoyé tel quel.
        // JeuPlacement stocke board[x][y] (Col, Ligne).
        model.addAttribute("board", game.getBoard());

        model.addAttribute("configRequise", game.getConfigurationRequise());
        model.addAttribute("compteActuel", game.getCompteActuelCalculated());
        boolean gagne = game.estPuzzleResolu();
        model.addAttribute("gagne", gagne);
    }
}