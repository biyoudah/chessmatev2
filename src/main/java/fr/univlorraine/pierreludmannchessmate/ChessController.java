package fr.univlorraine.pierreludmannchessmate;

import fr.univlorraine.pierreludmannchessmate.repository.UtilisateurRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur principal du jeu d'échecs.
 * <p>
 * Cette classe gère les interactions utilisateur avec le jeu d'échecs via le navigateur.
 * Elle orchestre :
 * <ul>
 * <li>Le mode "Bac à sable" (Show) pour placer des pièces librement.</li>
 * <li>Le mode "Puzzle" pour résoudre des situations tactiques.</li>
 * <li>La communication avec l'API externe pour récupérer des puzzles.</li>
 * </ul>
 * <p>
 * L'état du jeu est maintenu dans la session utilisateur via l'attribut "game".
 */
@Controller
@SessionAttributes("game")
public class ChessController {

    private final UtilisateurRepository utilisateurRepository;
    private final ChessApiService chessApiService;

    /**
     * Constructeur avec injection des dépendances.
     *
     * @param utilisateurRepository Repository pour accéder aux données des utilisateurs.
     * @param chessApiService Service pour communiquer avec l'API d'échecs externe.
     */
    public ChessController(UtilisateurRepository utilisateurRepository, ChessApiService chessApiService) {
        this.utilisateurRepository = utilisateurRepository;
        this.chessApiService = chessApiService;
    }

    /**
     * Initialise une nouvelle partie d'échecs pour la session.
     * Cette méthode est appelée automatiquement par Spring pour créer
     * l'attribut de session "game" s'il n'existe pas encore.
     *
     * @return Une nouvelle instance de ChessGame.
     */
    @ModelAttribute("game")
    public ChessGame initGame() {
        return new ChessGame();
    }

    /**
     * Gère la redirection depuis la racine vers la page d'accueil.
     *
     * @return Redirection vers la page d'accueil.
     */
    @GetMapping("/")
    public String root() {
        return "redirect:/home";
    }

    /**
     * Affiche la page d'accueil de l'application.
     *
     * @param model Le modèle pour la vue.
     * @param auth  Les informations d'authentification de l'utilisateur.
     * @return Le nom de la vue à afficher (home.html).
     */
    @GetMapping("/home")
    public String home(Model model, Authentication auth) {
        injecterInfosUtilisateur(model, auth);
        return "home";
    }

    /**
     * Affiche la vue principale du jeu d'échecs (Mode Bac à sable).
     *
     * @param game  L'instance du jeu stockée en session.
     * @param model Le modèle pour la vue.
     * @param auth  Les informations d'authentification de l'utilisateur.
     * @return Le nom de la vue à afficher (show.html).
     */
    @GetMapping("/show")
    public String getShow(@ModelAttribute("game") ChessGame game,
                          Model model, Authentication auth) {
        updateGameModel(model, game, auth);
        return "show";
    }

    /**
     * Traite la demande de placement d'une pièce sur le plateau (Mode Show).
     *
     * @param x         Coordonnée X de la case (0-7).
     * @param y         Coordonnée Y de la case (0-7).
     * @param pieceType Type de pièce à placer (Dame, Tour, Fou, etc.).
     * @param estBlanc  Indique si la pièce est blanche (true) ou noire (false).
     * @param game      L'instance du jeu stockée en session.
     * @param model     Le modèle pour la vue.
     * @param auth      Les informations d'authentification de l'utilisateur.
     * @return Le nom de la vue à afficher.
     */
    @PostMapping("/place")
    public String postPlace(@RequestParam int x, @RequestParam int y, @RequestParam String pieceType,
                            @RequestParam(defaultValue = "true") boolean estBlanc,
                            @ModelAttribute("game") ChessGame game,
                            Model model, Authentication auth) {
        String res = game.placerPiece(x, y, pieceType, estBlanc);
        if ("OCCUPEE".equals(res)) model.addAttribute("message", "❌ Case déjà occupée !");
        else if ("INVALID".equals(res)) model.addAttribute("message", "⚠️ Impossible : Case menacée !");

        updateGameModel(model, game, auth);
        return "show";
    }

    /**
     * Traite la demande de retrait d'une pièce du plateau (Mode Show).
     *
     * @param x     Coordonnée X de la case (0-7).
     * @param y     Coordonnée Y de la case (0-7).
     * @param game  L'instance du jeu stockée en session.
     * @param model Le modèle pour la vue.
     * @param auth  Les informations d'authentification de l'utilisateur.
     * @return Le nom de la vue à afficher.
     */
    @PostMapping("/remove")
    public String postRemove(@RequestParam int x, @RequestParam int y,
                             @ModelAttribute("game") ChessGame game,
                             Model model, Authentication auth) {
        game.retirerPiece(x, y);
        updateGameModel(model, game, auth);
        return "show";
    }

    /**
     * Affiche la page du mode Puzzle.
     * <p>
     * Si le plateau est vide ou non initialisé à l'arrivée sur la page,
     * un puzzle aléatoire est automatiquement récupéré via l'API Chess.com.
     *
     * @param game  L'instance du jeu stockée en session.
     * @param model Le modèle pour la vue.
     * @param auth  Les informations d'authentification de l'utilisateur.
     * @return Le nom de la vue "puzzle".
     */
    @GetMapping("/puzzle")
    public String getPuzzle(@ModelAttribute("game") ChessGame game,
                            Model model, Authentication auth) {

        if (estPlateauVideOuDefaut(game)) {
            genererPuzzleAleatoire(game);
        }

        model.addAttribute("traitAuBlanc", game.isTraitAuBlanc());
        updateGameModel(model, game, auth);
        return "puzzle";
    }

    /**
     * Gère le déplacement d'une pièce (Départ -> Arrivée) en mode Puzzle.
     * <p>
     * Cette méthode valide le mouvement en utilisant les règles spécifiques des pièces
     * (géométrie, obstacles) avant de l'appliquer sur le plateau.
     *
     * @param departX  Ligne de départ.
     * @param departY  Colonne de départ.
     * @param arriveeX Ligne d'arrivée.
     * @param arriveeY Colonne d'arrivée.
     * @param game     L'instance du jeu.
     * @param model    Le modèle pour la vue.
     * @param auth     Auth utilisateur.
     * @return Retourne sur la vue puzzle avec le message de résultat.
     */
    @PostMapping("/move")
    public String postMove(@RequestParam int departX, @RequestParam int departY,
                           @RequestParam int arriveeX, @RequestParam int arriveeY,
                           @ModelAttribute("game") ChessGame game,
                           Model model, Authentication auth) {

        String resultat = tenterDeplacerPiece(game, departX, departY, arriveeX, arriveeY);

        if ("OK".equals(resultat)) {
            model.addAttribute("message", "Coup joué : " + departX + "," + departY + " ➝ " + arriveeX + "," + arriveeY);
            // Changement de trait pour simuler le tour par tour
            game.setTraitAuBlanc(!game.isTraitAuBlanc());
        } else {
            model.addAttribute("message", "⚠️ " + resultat);
        }

        model.addAttribute("traitAuBlanc", game.isTraitAuBlanc());
        updateGameModel(model, game, auth);
        return "puzzle";
    }

    /**
     * Génère un nouveau puzzle aléatoire depuis l'API externe.
     * <p>
     * Cette méthode est déclenchée manuellement par l'utilisateur via le bouton "Nouveau Puzzle".
     *
     * @param game  L'instance du jeu.
     * @param model Le modèle.
     * @param auth  Auth utilisateur.
     * @return Retourne sur la vue puzzle avec une nouvelle position chargée.
     */
    @PostMapping("/api/puzzle")
    public String apiNewPuzzle(@ModelAttribute("game") ChessGame game,
                               Model model, Authentication auth) {

        genererPuzzleAleatoire(game);

        model.addAttribute("message", "✨ Puzzle Chess.com chargé !");
        model.addAttribute("traitAuBlanc", game.isTraitAuBlanc());
        updateGameModel(model, game, auth);
        return "puzzle";
    }

    /**
     * Réinitialise le jeu en cours (vide le plateau).
     *
     * @param game L'instance du jeu stockée en session.
     * @return Redirection vers la vue du jeu.
     */
    @PostMapping("/reset")
    public String postReset(@ModelAttribute("game") ChessGame game) {
        game.reinitialiser();
        return "redirect:/show";
    }

    /**
     * Change le mode de jeu actuel (pour le mode Show).
     *
     * @param modeDeJeu Le mode de jeu à activer.
     * @param game      L'instance du jeu stockée en session.
     * @param model     Le modèle pour la vue.
     * @param auth      Les informations d'authentification de l'utilisateur.
     * @return Le nom de la vue à afficher.
     */
    @PostMapping("/changeMode")
    public String postChangeMode(@RequestParam String modeDeJeu,
                                 @ModelAttribute("game") ChessGame game,
                                 Model model, Authentication auth) {
        if (!"custom".equals(modeDeJeu)) {
            configurerRegles(game, modeDeJeu);
            game.setModeDeJeu(modeDeJeu);
            game.reinitialiser();
        } else {
            game.setModeDeJeu("custom");
        }
        updateGameModel(model, game, auth);
        return "show";
    }

    /**
     * Configure un mode de jeu personnalisé (Show).
     *
     * @param params Map contenant les paramètres de configuration.
     * @param game   L'instance du jeu stockée en session.
     * @param model  Le modèle pour la vue.
     * @param auth   Les informations d'authentification de l'utilisateur.
     * @return Le nom de la vue à afficher.
     */
    @PostMapping("/customConfig")
    public String postCustomConfig(@RequestParam Map<String, String> params,
                                   @ModelAttribute("game") ChessGame game,
                                   Model model, Authentication auth) {
        Map<String, Integer> newConfig = new HashMap<>();
        String[] types = {"Dame", "Tour", "Fou", "Cavalier", "Roi", "Pion"};

        for (String t : types) {
            try {
                String val = params.get(t.toLowerCase());
                if (val != null && !val.isEmpty()) {
                    int nb = Integer.parseInt(val);
                    if (nb > 0) newConfig.put(t, nb);
                }
            } catch (NumberFormatException ignored) {
            }
        }

        String validation = game.validerConfiguration(newConfig);
        if (!"OK".equals(validation)) {
            model.addAttribute("message", "❌ " + validation);
        } else {
            game.setModeDeJeu("custom");
            game.setConfigurationRequise(newConfig);
            game.reinitialiser();
            model.addAttribute("message", "✅ Config personnalisée active !");
        }
        updateGameModel(model, game, auth);
        return "show";
    }

    /**
     * Appelle le service d'API pour récupérer un FEN aléatoire et charger le plateau.
     *
     * @param game L'instance du jeu à modifier.
     */
    private void genererPuzzleAleatoire(ChessGame game) {
        String fen = chessApiService.getRandomPuzzleFen();
        game.chargerFen(fen);
    }

    /**
     * Vérifie si le plateau est vide ou dans son état initial.
     *
     * @param game L'instance du jeu.
     * @return true si le plateau est vide, false sinon.
     */
    private boolean estPlateauVideOuDefaut(ChessGame game) {
        String[][] board = game.getBoard();
        return board[0][4] == null || board[0][4].isEmpty();
    }

    /**
     * Met à jour le modèle avec les données du jeu communes à toutes les vues.
     *
     * @param model Le modèle pour la vue.
     * @param game  L'instance du jeu stockée en session.
     * @param auth  Les informations d'authentification de l'utilisateur.
     */
    private void updateGameModel(Model model, ChessGame game, Authentication auth) {
        injecterInfosUtilisateur(model, auth);
        model.addAttribute("board", game.getBoard());
        model.addAttribute("configRequise", game.getConfigurationRequise());
        model.addAttribute("compteActuel", game.getCompteActuel());
        boolean gagne = game.estPuzzleResolu();
        model.addAttribute("gagne", gagne);
        if (gagne) model.addAttribute("message", "🏆 BRAVO ! Configuration réussie !");
    }

    /**
     * Configure les règles du jeu en fonction du mode sélectionné (Show).
     *
     * @param game L'instance du jeu à configurer.
     * @param mode Le mode de jeu à configurer.
     */
    private void configurerRegles(ChessGame game, String mode) {
        Map<String, Integer> config = new HashMap<>();
        switch (mode) {
            case "8-dames" -> config.put("Dame", 8);
            case "8-tours" -> config.put("Tour", 8);
            case "14-fous" -> config.put("Fou", 14);
            case "16-rois" -> config.put("Roi", 16);
            case "mix-dame-cavalier" -> {
                config.put("Dame", 5);
                config.put("Cavalier", 3);
            }
            default -> config.put("Dame", 8);
        }
        game.setConfigurationRequise(config);
    }

    /**
     * Injecte les informations de l'utilisateur dans le modèle.
     *
     * @param model          Le modèle pour la vue.
     * @param authentication Les informations d'authentification de l'utilisateur.
     */
    private void injecterInfosUtilisateur(Model model, Authentication authentication) {
        boolean isConnected = authentication != null && authentication.isAuthenticated() &&
                !(authentication instanceof AnonymousAuthenticationToken);

        model.addAttribute("isLoggedIn", isConnected);
        model.addAttribute("pseudo", isConnected ? authentication.getName() : "Invité");
    }

    /**
     * Tente de déplacer une pièce en vérifiant la validité du mouvement.
     * <p>
     * Vérifie l'existence de la pièce, la validité géométrique du mouvement
     * via la classe de la pièce, et l'absence d'obstacles sur le chemin.
     *
     * @param game     L'instance du jeu.
     * @param dX       X de départ.
     * @param dY       Y de départ.
     * @param aX       X d'arrivée.
     * @param aY       Y d'arrivée.
     * @return "OK" si le mouvement est valide, sinon un message d'erreur.
     */
    private String tenterDeplacerPiece(ChessGame game, int dX, int dY, int aX, int aY) {
        Piece piece = game.getPieceObject(dX, dY);

        if (piece == null) return "Case de départ vide !";

        if (!piece.deplacementValide(dX, dY, aX, aY)) {
            return "Mouvement invalide pour " + piece.getClass().getSimpleName();
        }

        if (!game.cheminLibre(dX, dY, aX, aY)) {
            return "Le chemin n'est pas libre !";
        }

        String type = piece.getClass().getSimpleName();
        boolean estBlanc = piece.estBlanc();

        game.retirerPiece(dX, dY);
        game.placerPiece(aX, aY, type, estBlanc);

        return "OK";
    }
}