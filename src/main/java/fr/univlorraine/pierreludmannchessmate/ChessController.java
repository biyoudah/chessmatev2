package fr.univlorraine.pierreludmannchessmate;

import fr.univlorraine.pierreludmannchessmate.model.Utilisateur;
import fr.univlorraine.pierreludmannchessmate.repository.UtilisateurRepository;
import org.springframework.security.core.Authentication;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@SessionAttributes("game")
public class ChessController {

    private final UtilisateurRepository utilisateurRepository;

    public ChessController(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @ModelAttribute("game")
    ChessGame createGame() {
        ChessGame game = new ChessGame();
        //og.info("Create new chess puzzle game");
        return game;
    }

    @GetMapping("/")
    String getHome() {
        return "redirect:/home";
    }

    @GetMapping("/new")
    String getNew() {
        return "new";
    }

    @PostMapping("/create")
    public String postCreate(@RequestParam(required = false) String pseudo,
                             @RequestParam String modeDeJeu,
                             Model model) {
        ChessGame game;
        if (pseudo != null && !pseudo.isEmpty()) {
            game = new ChessGame(pseudo);
        } else {
            game = new ChessGame();
        }
        game.setModeDeJeu(modeDeJeu);
        model.addAttribute("game", game);
        return "redirect:/show";
    }

    // --- ACTION : PLACER UNE PIÈCE ---
    @PostMapping("/place")
    String postPlace(@RequestParam int x,
                     @RequestParam int y,
                     @RequestParam String pieceType,
                     @RequestParam(defaultValue = "true") boolean estBlanc,
                     RedirectAttributes redirAttrs,
                     @ModelAttribute("game") ChessGame game) {

        // Appel de la méthode qui retourne un statut (String)
        String resultat = game.placerPiece(x, y, pieceType, estBlanc);

        switch (resultat) {
            case "OCCUPEE":
                redirAttrs.addFlashAttribute("message", "❌ Impossible : la case est déjà occupée !");
                break;

            case "INVALID":
                redirAttrs.addFlashAttribute("message", "⚠️ Mauvais placement ! Cette case est menacée par une autre pièce.");
                break;

            case "OK":
                // Si le placement est valide, on vérifie si c'est la victoire
                if (game.estPuzzleResolu()) {
                    redirAttrs.addFlashAttribute("message", "🏆 BRAVO ! Vous avez placé les 8 Reines sans conflit !");
                } else {
                    redirAttrs.addFlashAttribute("message", "✅ Pièce placée.");
                }
                break;

            default:
                redirAttrs.addFlashAttribute("message", "Erreur technique lors du placement.");
        }

        return "redirect:/show";
    }

    // --- ACTION : RETIRER UNE PIÈCE ---
    @PostMapping("/remove")
    String postRemove(@RequestParam int x,
                      @RequestParam int y,
                      RedirectAttributes redirAttrs,
                      @ModelAttribute("game") ChessGame game) {

        // Appel de la méthode qui retourne true/false
        boolean success = game.retirerPiece(x, y);

        if (!success) {
            // Cas où on clique "Retirer" sur une case vide
            redirAttrs.addFlashAttribute("message", "❌ La case est déjà vide, rien à retirer.");
        } else {
            redirAttrs.addFlashAttribute("message", "🗑️ Pièce retirée.");
        }

        return "redirect:/show";
    }

    @PostMapping("/reset")
    String postReset(
            @ModelAttribute("game") ChessGame game) {
        game.reinitialiser();
        return "redirect:/show";
    }

    @GetMapping("/show")
    String getShow(@ModelAttribute("game") ChessGame game,
                   Model model,
                   Authentication authentication) { // Injection correcte

        String email = authentication.getName();

        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé !"));

        model.addAttribute("board", game.getBoard());
        model.addAttribute("pseudo", utilisateur.getPseudo()); // Pseudo disponible dans la vue
        model.addAttribute("nbPieces", game.compterPieces());
        model.addAttribute("score", game.getScore());
        return "show";
    }
    @GetMapping("/home")
    public String getHomePage(@ModelAttribute("game") ChessGame game,
                          Model model,
                          Authentication authentication) {

        boolean isLoggedIn = authentication != null && authentication.isAuthenticated();
        model.addAttribute("isLoggedIn", isLoggedIn);

        if (isLoggedIn) {
            String email = authentication.getName();
            Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            model.addAttribute("pseudo", utilisateur.getPseudo());
        }

        return "home";  // home.html
    }

}