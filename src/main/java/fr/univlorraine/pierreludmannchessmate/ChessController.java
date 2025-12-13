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
 * Cette classe gère les interactions utilisateur avec le jeu d'échecs,
 * notamment l'affichage du plateau, le placement des pièces, et la gestion
 * des différents modes de jeu (Huit dames, Huit tours, etc.).
 * Elle maintient l'état du jeu dans la session utilisateur.
 */
@Controller
@SessionAttributes("game")
public class ChessController {

    private final UtilisateurRepository utilisateurRepository;

    /**
     * Constructeur avec injection du repository des utilisateurs.
     * 
     * @param utilisateurRepository Repository pour accéder aux données des utilisateurs
     */
    public ChessController(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    /**
     * Initialise une nouvelle partie d'échecs pour la session.
     * Cette méthode est appelée automatiquement par Spring pour créer
     * l'attribut de session "game".
     * 
     * @return Une nouvelle instance de ChessGame
     */
    @ModelAttribute("game")
    public ChessGame initGame() {
        return new ChessGame();
    }

    /**
     * Gère la redirection depuis la racine vers la page d'accueil.
     * 
     * @return Redirection vers la page d'accueil
     */
    @GetMapping("/")
    public String root() {
        return "redirect:/home";
    }

    /**
     * Affiche la page d'accueil de l'application.
     * Injecte les informations de l'utilisateur connecté dans le modèle.
     * 
     * @param model Le modèle pour la vue
     * @param auth Les informations d'authentification de l'utilisateur
     * @return Le nom de la vue à afficher
     */
    @GetMapping("/home")
    public String home(Model model, Authentication auth) {
        // IMPORTANT : On doit injecter isLoggedIn ici aussi pour éviter le crash
        injecterInfosUtilisateur(model, auth);
        return "home";
    }

    /**
     * Affiche la vue principale du jeu d'échecs.
     * Prépare le modèle avec les données nécessaires pour afficher le plateau et l'état du jeu.
     * 
     * @param game L'instance du jeu stockée en session
     * @param model Le modèle pour la vue
     * @param auth Les informations d'authentification de l'utilisateur
     * @return Le nom de la vue à afficher
     */
    @GetMapping("/show")
    public String getShow(@ModelAttribute("game") ChessGame game,
                          Model model, Authentication auth) {
        updateGameModel(model, game, auth);
        return "show";
    }


    /**
     * Traite la demande de placement d'une pièce sur le plateau.
     * Vérifie si le placement est valide et ajoute un message approprié au modèle.
     * 
     * @param x Coordonnée X de la case (0-7)
     * @param y Coordonnée Y de la case (0-7)
     * @param pieceType Type de pièce à placer (Dame, Tour, Fou, etc.)
     * @param estBlanc Indique si la pièce est blanche (true) ou noire (false)
     * @param game L'instance du jeu stockée en session
     * @param model Le modèle pour la vue
     * @param auth Les informations d'authentification de l'utilisateur
     * @return Le nom de la vue à afficher
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
     * Traite la demande de retrait d'une pièce du plateau.
     * 
     * @param x Coordonnée X de la case (0-7)
     * @param y Coordonnée Y de la case (0-7)
     * @param game L'instance du jeu stockée en session
     * @param model Le modèle pour la vue
     * @param auth Les informations d'authentification de l'utilisateur
     * @return Le nom de la vue à afficher
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
     * Réinitialise le jeu en cours.
     * Vide le plateau et remet les compteurs à zéro.
     * 
     * @param game L'instance du jeu stockée en session
     * @return Redirection vers la vue du jeu
     */
    @PostMapping("/reset")
    public String postReset(@ModelAttribute("game") ChessGame game) {
        game.reinitialiser();
        return "redirect:/show";
    }


    /**
     * Change le mode de jeu actuel.
     * Configure les règles spécifiques au mode sélectionné et réinitialise le plateau.
     * 
     * @param modeDeJeu Le mode de jeu à activer (8-dames, 8-tours, 14-fous, 16-rois, mix-dame-cavalier, custom)
     * @param game L'instance du jeu stockée en session
     * @param model Le modèle pour la vue
     * @param auth Les informations d'authentification de l'utilisateur
     * @return Le nom de la vue à afficher
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
     * Configure un mode de jeu personnalisé.
     * Permet à l'utilisateur de définir le nombre de pièces de chaque type à placer.
     * Valide la configuration et affiche un message approprié.
     * 
     * @param params Map contenant les paramètres de configuration (nombre de pièces par type)
     * @param game L'instance du jeu stockée en session
     * @param model Le modèle pour la vue
     * @param auth Les informations d'authentification de l'utilisateur
     * @return Le nom de la vue à afficher
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
            } catch (NumberFormatException ignored) {}
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
     * Met à jour le modèle avec les données du jeu.
     * Prépare toutes les données nécessaires pour l'affichage du plateau et de l'état du jeu.
     * Vérifie également si le puzzle est résolu et ajoute un message de félicitation si c'est le cas.
     * 
     * @param model Le modèle pour la vue
     * @param game L'instance du jeu stockée en session
     * @param auth Les informations d'authentification de l'utilisateur
     */
    private void updateGameModel(Model model, ChessGame game, Authentication auth) {
        injecterInfosUtilisateur(model, auth); // Appelle la méthode corrigée ci-dessous
        model.addAttribute("board", game.getBoard());
        model.addAttribute("configRequise", game.getConfigurationRequise());
        model.addAttribute("compteActuel", game.getCompteActuel());
        boolean gagne = game.estPuzzleResolu();
        model.addAttribute("gagne", gagne);
        if (gagne) model.addAttribute("message", "🏆 BRAVO ! Configuration réussie !");
    }

    /**
     * Configure les règles du jeu en fonction du mode sélectionné.
     * Définit le nombre et le type de pièces requises pour chaque mode de jeu.
     * 
     * @param game L'instance du jeu à configurer
     * @param mode Le mode de jeu à configurer (8-dames, 8-tours, etc.)
     */
    private void configurerRegles(ChessGame game, String mode) {
        Map<String, Integer> config = new HashMap<>();
        switch (mode) {
            case "8-dames" -> config.put("Dame", 8);
            case "8-tours" -> config.put("Tour", 8);
            case "14-fous" -> config.put("Fou", 14);
            case "16-rois" -> config.put("Roi", 16);
            case "mix-dame-cavalier" -> { config.put("Dame", 5); config.put("Cavalier", 3); }
            default -> config.put("Dame", 8);
        }
        game.setConfigurationRequise(config);
    }

    /**
     * Injecte les informations de l'utilisateur dans le modèle.
     * Détermine si l'utilisateur est connecté et ajoute son pseudo au modèle.
     * Cette méthode est critique pour le bon fonctionnement des templates.
     * 
     * @param model Le modèle pour la vue
     * @param authentication Les informations d'authentification de l'utilisateur
     */
    private void injecterInfosUtilisateur(Model model, Authentication authentication) {
        boolean isConnected = authentication != null && authentication.isAuthenticated() &&
                !(authentication instanceof AnonymousAuthenticationToken);

        // On ajoute la variable manquante qui faisait planter le template home.html
        model.addAttribute("isLoggedIn", isConnected);

        model.addAttribute("pseudo", isConnected ? authentication.getName() : "Invité");
    }
}
