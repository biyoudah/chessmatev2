package fr.univlorraine.pierreludmannchessmate.controller;

import fr.univlorraine.pierreludmannchessmate.JeuPlacement;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/placement") // Préfixe pour toutes les URLs
@SessionAttributes("jeuPlacement") // Nom spécifique en session
public class PlacementController {

    // On initialise spécifiquement un JeuPlacement
    @ModelAttribute("jeuPlacement")
    public JeuPlacement initGame() {
        return new JeuPlacement();
    }

    @GetMapping
    public String showBoard(@ModelAttribute("jeuPlacement") JeuPlacement game, Model model, Authentication auth) {
        injecterInfosUtilisateur(model, auth);
        preparerModele(model, game);
        return "jeuPlacement";
    }

    @PostMapping("/reset")
    public String reset(SessionStatus status) {
        status.setComplete(); // Vide la session pour ce contrôleur
        return "redirect:/placement";
    }

    @PostMapping("/action")
    public String action(@RequestParam int x, @RequestParam int y,
                         @RequestParam(required = false) String type, // type peut être null si on retire
                         @ModelAttribute("jeuPlacement") JeuPlacement game,
                         RedirectAttributes redirectAttributes) {

        // Si la case est vide, on essaie de placer
        if (game.getPieceObject(x, y) == null) {
            if (type != null && !type.isEmpty()) {
                String resultat = game.placerPieceJoueur(x, y, type, true);
                if (!"OK".equals(resultat)) {
                    // On envoie l'erreur à la vue via FlashAttribute
                    redirectAttributes.addFlashAttribute("message", "⚠️ " + resultat);
                }
            }
        } else {
            // Si la case est occupée, on retire
            game.retirerPiece(x, y);
        }
        return "redirect:/placement";
    }

    /**
     * Change le mode de jeu (ex: 8-dames, 14-fous...)
     */
    @PostMapping("/changeMode")
    public String changeMode(@RequestParam("modeDeJeu") String mode,
                             @ModelAttribute("jeuPlacement") JeuPlacement game,
                             RedirectAttributes redirectAttributes) {

        Map<String, Integer> config = new HashMap<>();
        switch (mode) {
            case "8-dames" -> config.put("Dame", 8);
            case "8-tours" -> config.put("Tour", 8);
            case "14-fous" -> config.put("Fou", 14);
            case "16-rois" -> config.put("Roi", 16);
            case "custom" -> { /* Ne rien faire, attend la config custom */ }
            default -> config.put("Dame", 8);
        }

        if (!"custom".equals(mode)) {
            game.setConfigurationRequise(config);
            game.setModeDeJeu(mode); // Assurez-vous d'avoir ce champ dans JeuPlacement
            game.reinitialiser(); // Vide le plateau
            redirectAttributes.addFlashAttribute("message", "Mode changé : " + mode);
        } else {
            game.setModeDeJeu("custom");
        }

        return "redirect:/placement";
    }

    /**
     * Configure un mode personnalisé via le formulaire
     */
    @PostMapping("/customConfig")
    public String customConfig(@RequestParam Map<String, String> params,
                               @ModelAttribute("jeuPlacement") JeuPlacement game,
                               RedirectAttributes redirectAttributes) {

        Map<String, Integer> newConfig = new HashMap<>();
        // Liste des clés attendues (noms des pièces en minuscules dans le formulaire ?)
        // Attention : votre formulaire HTML envoie c_dame, c_tour etc ? Ou juste dame, tour ?
        // Adaptez selon les 'name' de vos inputs HTML.
        // Supposons que les inputs sont : c_dame, c_tour...

        parseConfigParam(params, newConfig, "c_dame", "Dame");
        parseConfigParam(params, newConfig, "c_tour", "Tour");
        parseConfigParam(params, newConfig, "c_fou", "Fou");
        parseConfigParam(params, newConfig, "c_cavalier", "Cavalier");
        parseConfigParam(params, newConfig, "c_roi", "Roi");

        // Validation (méthode à avoir dans JeuPlacement)
        String validation = game.validerConfiguration(newConfig);

        if ("OK".equals(validation)) {
            game.setConfigurationRequise(newConfig);
            game.setModeDeJeu("custom");
            game.reinitialiser();
            redirectAttributes.addFlashAttribute("message", "✅ Configuration personnalisée appliquée !");
        } else {
            redirectAttributes.addFlashAttribute("message", "❌ Erreur config : " + validation);
        }

        return "redirect:/placement";
    }

    // --- Méthodes Utilitaires ---

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
        model.addAttribute("isLoggedIn", auth != null && auth.isAuthenticated());
    }

    private void preparerModele(Model model, JeuPlacement game) {
        model.addAttribute("board", game.getBoard());
        model.addAttribute("configRequise", game.getConfigurationRequise());
        model.addAttribute("compteActuel", game.getCompteActuelCalculated());

        boolean gagne = game.estPuzzleResolu();
        model.addAttribute("gagne", gagne);

        // Si un message flash n'est pas déjà présent
        if (!model.containsAttribute("message")) {
            model.addAttribute("message", gagne ? "✅ GAGNÉ !" : "À vous de jouer");
        }
    }
}