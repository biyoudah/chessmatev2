package fr.univlorraine.pierreludmannchessmate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@SessionAttributes("game")
public class ChessController {

    @ModelAttribute("game")
    ChessGame createGame() {
        ChessGame game = new ChessGame();
        log.info("Create new chess puzzle game");
        return game;
    }

    @GetMapping("/")
    String getHome() {
        return "redirect:/new";
    }

    @GetMapping("/new")
    String getNew() {
        return "new";
    }

    @PostMapping("/create")
    String postCreate(@RequestParam(required = false) String pseudo, Model model) {
        ChessGame game;
        if (pseudo != null && !pseudo.isEmpty()) {
            game = new ChessGame(pseudo);
        } else {
            game = new ChessGame();
        }
        model.addAttribute("game", game);
        return "redirect:/show";
    }

    // Placer une pièce
    @PostMapping("/place")
    String postPlace(@RequestParam int x,
                     @RequestParam int y,
                     @RequestParam String pieceType,
                     @RequestParam(defaultValue = "true") boolean estBlanc,
                     RedirectAttributes redirAttrs,
                     @SessionAttribute("game") ChessGame game) {

        boolean success = game.placerPiece(x, y, pieceType, estBlanc);

        if (!success) {
            redirAttrs.addFlashAttribute("message", "Impossible de placer la pièce ici");
        } else {
            redirAttrs.addFlashAttribute("message", "Pièce placée avec succès");
        }

        return "redirect:/show";
    }

    // Retirer une pièce
    @PostMapping("/remove")
    String postRemove(@RequestParam int x,
                      @RequestParam int y,
                      RedirectAttributes redirAttrs,
                      @SessionAttribute("game") ChessGame game) {

        boolean success = game.retirerPiece(x, y);

        if (!success) {
            redirAttrs.addFlashAttribute("message", "Aucune pièce à retirer");
        } else {
            redirAttrs.addFlashAttribute("message", "Pièce retirée");
        }

        return "redirect:/show";
    }

    // Réinitialiser
    @PostMapping("/reset")
    String postReset(@SessionAttribute("game") ChessGame game) {
        game.reinitialiser();
        return "redirect:/show";
    }

    @GetMapping("/show")
    String getShow(@ModelAttribute("game") ChessGame game, Model model) {
        model.addAttribute("board", game.getBoard());
        model.addAttribute("joueur", game.getJoueur().getPseudo());
        model.addAttribute("nbPieces", game.compterPieces());
        model.addAttribute("score", game.getScore());

        return "show";
    }
}
