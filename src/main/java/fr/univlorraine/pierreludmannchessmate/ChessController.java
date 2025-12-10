package fr.univlorraine.pierreludmannchessmate;

import fr.univlorraine.pierreludmannchessmate.model.Utilisateur;
import fr.univlorraine.pierreludmannchessmate.repository.UtilisateurRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
<<<<<<< HEAD
=======

>>>>>>> origin/main
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

<<<<<<< HEAD
=======

>>>>>>> origin/main
@Controller
@SessionAttributes("game")
public class ChessController {

    private final UtilisateurRepository utilisateurRepository;

    // Client API Chess.com
    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.chess.com/pub")
            .build();

    public ChessController(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    // Initialisation de la session "game"
    @ModelAttribute("game")
    ChessGame createGame() {
<<<<<<< HEAD
        return new ChessGame();
=======
        ChessGame game = new ChessGame();
        //og.info("Create new chess puzzle game");
        return game;
>>>>>>> origin/main
    }

    // --- NAVIGATION ---

    @GetMapping("/")
<<<<<<< HEAD
    public String root() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String getHomePage(Model model, Authentication authentication) {
        injecterInfosUtilisateur(model, authentication);
        return "home";
=======
    String getHome() {
        return "redirect:/home";
>>>>>>> origin/main
    }

    @GetMapping("/new")
    public String getNew(Model model, Authentication authentication) {
        injecterInfosUtilisateur(model, authentication);
        return "new";
    }

    // --- CRÉATION DE PARTIE ---

    @PostMapping("/create")
    public String postCreate(@RequestParam(required = false) String pseudo,
                             @RequestParam String modeDeJeu,
                             Model model) {

        // Si pas de pseudo, on met "Invité" ou celui du constructeur par défaut
        ChessGame game = (pseudo != null && !pseudo.isEmpty()) ? new ChessGame(pseudo) : new ChessGame("Invité");

        game.setModeDeJeu(modeDeJeu);
        model.addAttribute("game", game);

<<<<<<< HEAD
        if ("puzzle".equals(modeDeJeu)) {
            return "redirect:/puzzle";
=======
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

            case "MENACANT":
                redirAttrs.addFlashAttribute("message", "⚠️ Mauvais placement ! Cette case menace une autre pièce.");
                break;

            default:
                redirAttrs.addFlashAttribute("message", "Erreur technique lors du placement.");
>>>>>>> origin/main
        }
        return "redirect:/show";
    }

    // =========================================================================
    // MODE 8 REINES (SHOW)
    // =========================================================================

    @GetMapping("/show")
    public String getShow(@ModelAttribute("game") ChessGame game, Model model, Authentication authentication) {
        injecterInfosUtilisateur(model, authentication);

        model.addAttribute("board", game.getBoard());
        model.addAttribute("nbPieces", game.compterPieces());
        model.addAttribute("score", game.getScore());
        return "show";
    }
<<<<<<< HEAD

    @PostMapping("/place")
    public String postPlace(@RequestParam int x, @RequestParam int y, @RequestParam String pieceType,
                            @RequestParam(defaultValue = "true") boolean estBlanc,
                            RedirectAttributes redirAttrs, @ModelAttribute("game") ChessGame game) {

        String resultat = game.placerPiece(x, y, pieceType, estBlanc);
        switch (resultat) {
            case "OCCUPEE" -> redirAttrs.addFlashAttribute("message", "❌ Case occupée !");
            case "INVALID" -> redirAttrs.addFlashAttribute("message", "⚠️ Case menacée !");
            case "OK" -> {
                if (game.estPuzzleResolu()) redirAttrs.addFlashAttribute("message", "🏆 BRAVO ! Gagné !");
                else redirAttrs.addFlashAttribute("message", "✅ Pièce placée.");
            }
            case "MENACANT" -> redirAttrs.addFlashAttribute("message", "⚠️ Menace une autre pièce.");
            default -> redirAttrs.addFlashAttribute("message", "Erreur.");
        }
        return "redirect:/show";
    }

    @PostMapping("/remove")
    public String postRemove(@RequestParam int x, @RequestParam int y,
                             RedirectAttributes redirAttrs, @ModelAttribute("game") ChessGame game) {
        if (game.retirerPiece(x, y)) redirAttrs.addFlashAttribute("message", "🗑️ Pièce retirée.");
        else redirAttrs.addFlashAttribute("message", "❌ Case déjà vide.");
        return "redirect:/show";
    }

    // =========================================================================
    // MODE PUZZLE API (PUZZLE)
    // =========================================================================

    @GetMapping("/puzzle")
    public String getPuzzlePage(@ModelAttribute("game") ChessGame game, Model model, Authentication authentication) {
        injecterInfosUtilisateur(model, authentication);

        model.addAttribute("board", game.getBoard());
        model.addAttribute("score", game.getScore());
        model.addAttribute("traitAuBlanc", game.isTraitAuBlanc());
        return "puzzle";
    }

    @PostMapping("/api/puzzle")
    public String loadApiPuzzle(@ModelAttribute("game") ChessGame game, RedirectAttributes redirAttrs) {
        try {
            DailyPuzzle puzzle = restClient.get()
                    .uri("/puzzle/random")
                    .retrieve()
                    .body(DailyPuzzle.class);

            if (puzzle != null) {
                game.chargerDepuisFen(puzzle.fen());
                game.setSolutionPuzzle(puzzle.pgn());
                game.setModeDeJeu("puzzle-api");
                redirAttrs.addFlashAttribute("message", "🧩 Puzzle chargé : " + puzzle.title());
            }
        } catch (Exception e) {
            redirAttrs.addFlashAttribute("message", "❌ Erreur API Chess.com");
        }
        return "redirect:/puzzle";
    }

    @PostMapping("/move")
    public String postMove(@RequestParam int departX, @RequestParam int departY,
                           @RequestParam int arriveeX, @RequestParam int arriveeY,
                           RedirectAttributes redirAttrs,
                           @ModelAttribute("game") ChessGame game) {

        String resultat = game.deplacerPiece(departX, departY, arriveeX, arriveeY);

        if ("OK".equals(resultat)) {
            redirAttrs.addFlashAttribute("message", "Coup joué !");
        } else {
            redirAttrs.addFlashAttribute("message", "⚠️ Mouvement impossible : " + resultat);
        }
        return "redirect:/puzzle";
    }

    @PostMapping("/reset")
    public String postReset(@ModelAttribute("game") ChessGame game, RedirectAttributes attrs) {
        game.reinitialiser();
        attrs.addFlashAttribute("message", "Plateau réinitialisé.");
        if ("puzzle-api".equals(game.getModeDeJeu()) || "puzzle".equals(game.getModeDeJeu())) {
            return "redirect:/puzzle";
        }
        return "redirect:/show";
    }

    // =========================================================================
    // MÉTHODE D'AIDE (Gère le cas connecté OU non connecté)
    // =========================================================================

    private void injecterInfosUtilisateur(Model model, Authentication authentication) {
        // Vérifie si l'objet authentication existe et que ce n'est pas un utilisateur anonyme
        if (authentication != null && authentication.isAuthenticated() &&
                !(authentication instanceof AnonymousAuthenticationToken)) {

            model.addAttribute("isLoggedIn", true);
            String email = authentication.getName();

            // On récupère le pseudo depuis la BDD
            utilisateurRepository.findByEmail(email).ifPresentOrElse(
                    u -> model.addAttribute("pseudo", u.getPseudo()),
                    () -> model.addAttribute("pseudo", "Utilisateur")
            );

        } else {
            // Cas NON CONNECTÉ (Invité)
            model.addAttribute("isLoggedIn", false);
            model.addAttribute("pseudo", "Invité 🕵️");
        }
    }

    // DTO pour l'API
    public record DailyPuzzle(String title, String fen, String pgn, String image) {}
=======
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

>>>>>>> origin/main
}