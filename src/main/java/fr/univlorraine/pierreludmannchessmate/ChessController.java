package fr.univlorraine.pierreludmannchessmate;

import fr.univlorraine.pierreludmannchessmate.model.Utilisateur;
import fr.univlorraine.pierreludmannchessmate.repository.UtilisateurRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@SessionAttributes("game")
public class ChessController {

    private final UtilisateurRepository utilisateurRepository;

    // Client HTTP pour Chess.com
    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.chess.com/pub")
            .build();

    public ChessController(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @ModelAttribute("game")
    ChessGame createGame() {
        return new ChessGame();
    }

    // --- NAVIGATION DE BASE ---

    @GetMapping("/")
    String getHome() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String getHomePage(Model model, Authentication authentication) {
        injecterInfosUtilisateur(model, authentication);
        return "home";
    }

    @GetMapping("/new")
    String getNew() {
        return "new";
    }

    // --- CRÉATION DE PARTIE ---

    @PostMapping("/create")
    public String postCreate(@RequestParam(required = false) String pseudo,
                             @RequestParam String modeDeJeu,
                             Model model) {
        ChessGame game = (pseudo != null && !pseudo.isEmpty()) ? new ChessGame(pseudo) : new ChessGame();
        game.setModeDeJeu(modeDeJeu);
        model.addAttribute("game", game);

        // Si on choisit le mode Puzzle, on redirige vers la page puzzle, sinon vers show (8 reines)
        if ("puzzle".equals(modeDeJeu)) {
            return "redirect:/puzzle";
        }
        return "redirect:/show";
    }

    // =========================================================================
    // SECTION 1 : MODE PLACEMENT (8 REINES / SHOW.HTML)
    // =========================================================================

    @GetMapping("/show")
    String getShow(@ModelAttribute("game") ChessGame game, Model model, Authentication authentication) {
        injecterInfosUtilisateur(model, authentication);
        model.addAttribute("board", game.getBoard());
        model.addAttribute("nbPieces", game.compterPieces());
        model.addAttribute("score", game.getScore());
        return "show";
    }

    @PostMapping("/place")
    String postPlace(@RequestParam int x, @RequestParam int y, @RequestParam String pieceType,
                     @RequestParam(defaultValue = "true") boolean estBlanc,
                     RedirectAttributes redirAttrs, @ModelAttribute("game") ChessGame game) {

        String resultat = game.placerPiece(x, y, pieceType, estBlanc);
        switch (resultat) {
            case "OCCUPEE" -> redirAttrs.addFlashAttribute("message", "❌ Case occupée !");
            case "INVALID" -> redirAttrs.addFlashAttribute("message", "⚠️ Case menacée !");
            case "OK" -> {
                if (game.estPuzzleResolu()) redirAttrs.addFlashAttribute("message", "🏆 BRAVO ! 8 Reines placées !");
                else redirAttrs.addFlashAttribute("message", "✅ Pièce placée.");
            }
            case "MENACANT" -> redirAttrs.addFlashAttribute("message", "⚠️ Menace une autre pièce.");
            default -> redirAttrs.addFlashAttribute("message", "Erreur.");
        }
        return "redirect:/show";
    }

    @PostMapping("/remove")
    String postRemove(@RequestParam int x, @RequestParam int y,
                      RedirectAttributes redirAttrs, @ModelAttribute("game") ChessGame game) {
        if (game.retirerPiece(x, y)) redirAttrs.addFlashAttribute("message", "🗑️ Pièce retirée.");
        else redirAttrs.addFlashAttribute("message", "❌ Case déjà vide.");
        return "redirect:/show";
    }

    // =========================================================================
    // SECTION 2 : MODE PUZZLE API (PUZZLE.HTML) - C'EST ÇA QUI MANQUAIT
    // =========================================================================

    // 1. Afficher la page Puzzle (GET)
    @GetMapping("/puzzle")
    public String getPuzzlePage(@ModelAttribute("game") ChessGame game, Model model, Authentication authentication) {
        injecterInfosUtilisateur(model, authentication);
        model.addAttribute("board", game.getBoard());
        model.addAttribute("score", game.getScore());
        // Ajoute le trait (c'est aux blancs ou aux noirs de jouer ?)
        model.addAttribute("traitAuBlanc", game.getJoueur().estBlanc());
        return "puzzle";
    }

    // 2. Charger un Puzzle depuis l'API (POST)
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
            e.printStackTrace();
        }
        // IMPORTANT : Redirige vers /puzzle, pas /show
        return "redirect:/puzzle";
    }

    @PostMapping("/move")
    public String postMove(@RequestParam int departX, @RequestParam int departY,
                           @RequestParam int arriveeX, @RequestParam int arriveeY,
                           RedirectAttributes redirAttrs,
                           @ModelAttribute("game") ChessGame game) {

        // --- DEBUG : Affiche ce que le serveur reçoit ---
        System.out.println("--- TENTATIVE DE MOUVEMENT ---");
        System.out.println("Départ : (" + departX + ", " + departY + ")");
        System.out.println("Arrivée : (" + arriveeX + ", " + arriveeY + ")");

        // Vérifions ce qu'il y a sur la case de départ AVANT le move
        // Attention : il faut que ta méthode getCase soit accessible ou passer par getBoard
        // Ici on suppose que tu as accès à echiquier ou une méthode de debug
        // System.out.println("Pièce détectée : " + game.getEchiquier().getCase(departX, departY).getPiece());

        String resultat = game.deplacerPiece(departX, departY, arriveeX, arriveeY);

        System.out.println("Résultat du mouvement : " + resultat);
        System.out.println("------------------------------");

        if ("OK".equals(resultat)) {
            redirAttrs.addFlashAttribute("message", "Coup joué !");
        } else {
            // On renvoie le message d'erreur précis à l'écran
            redirAttrs.addFlashAttribute("message", "⚠️ Mouvement impossible : " + resultat);
        }

        return "redirect:/puzzle";
    }
    // =========================================================================
    // UTILITAIRES
    // =========================================================================

    @PostMapping("/reset")
    String postReset(@ModelAttribute("game") ChessGame game, RedirectAttributes attrs) {
        game.reinitialiser();
        attrs.addFlashAttribute("message", "Plateau réinitialisé.");
        // Redirige intelligemment selon le mode actuel
        if ("puzzle-api".equals(game.getModeDeJeu())) {
            return "redirect:/puzzle";
        }
        return "redirect:/show";
    }

    // Helper pour ne pas répéter le code d'authentification 4 fois
    private void injecterInfosUtilisateur(Model model, Authentication authentication) {
        boolean isLoggedIn = authentication != null && authentication.isAuthenticated();
        model.addAttribute("isLoggedIn", isLoggedIn);

        if (isLoggedIn) {
            String email = authentication.getName();
            utilisateurRepository.findByEmail(email).ifPresent(u ->
                    model.addAttribute("pseudo", u.getPseudo())
            );
        }
    }

    // DTO interne pour l'API JSON
    public record DailyPuzzle(String title, String fen, String pgn, String image) {}
}