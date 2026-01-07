package fr.univlorraine.pierreludmannchessmate.controller;

import fr.univlorraine.pierreludmannchessmate.JeuPlacement;
import fr.univlorraine.pierreludmannchessmate.model.Score;
import fr.univlorraine.pierreludmannchessmate.model.Utilisateur;
import fr.univlorraine.pierreludmannchessmate.repository.ScoreRepository;
import fr.univlorraine.pierreludmannchessmate.repository.UtilisateurRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

@Controller
@RequestMapping("/placement")
@SessionAttributes("jeuPlacement")
public class PlacementController {

    private final UtilisateurRepository utilisateurRepository;
    private final ScoreRepository scoreRepository;

    public PlacementController(UtilisateurRepository utilisateurRepository, ScoreRepository scoreRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.scoreRepository = scoreRepository;
    }

    @ModelAttribute("jeuPlacement")
    public JeuPlacement initPlacement() {
        return new JeuPlacement();
    }

    @GetMapping
    public String afficherPlacement(@ModelAttribute("jeuPlacement") JeuPlacement game,
                                    Model model,
                                    Authentication auth,
                                    HttpSession session) {

        Object msg = session.getAttribute("flashMessage");
        if (msg != null) {
            model.addAttribute("message", msg);
            session.removeAttribute("flashMessage");
        }

        injecterInfosUtilisateur(model, auth);

        // --- C'est ici qu'on injecte le classement ---
        injecterClassement(model, game);

        preparerModele(model, game);
        return "jeuPlacement";
    }

    @PostMapping("/reset")
    public String reset(@ModelAttribute("jeuPlacement") JeuPlacement game,
                        SessionStatus status, HttpSession session) {
        game.reinitialiser();
        game.setScoreEnregistre(false);
        session.setAttribute("flashMessage", "Plateau réinitialisé.");
        return "redirect:/placement";
    }

    @PostMapping("/action")
    public String action(@RequestParam int x, @RequestParam int y,
                         @RequestParam(required = false) String type,
                         @ModelAttribute("jeuPlacement") JeuPlacement game,
                         HttpSession session,
                         Authentication auth) {

        if (game.getPieceObject(x, y) == null) {
            // Tentative de Placement
            if (type != null && !type.isEmpty()) {
                String resultat = game.placerPieceJoueur(x, y, type, true);

                if (!"OK".equals(resultat)) {
                    // --- AJOUT IMPORTANT ---
                    game.incrementerErreurs(); // On compte la faute dans le modèle

                    String msgErreur = "INVALID".equals(resultat) ? "Impossible : Case menacée !" : "Erreur : " + resultat;
                    session.setAttribute("flashMessage", "⚠️ " + msgErreur);
                }
            }
        } else {
            // Gomme
            game.retirerPiece(x, y);
        }

        if (game.estPuzzleResolu()) {
            traiterVictoireEtEnregistrerScore(game, session, auth);
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
            case "custom" -> { }
            default -> config.put("Dame", 8);
        }

        if (!"custom".equals(mode)) {
            game.setConfigurationRequise(config);
            game.setModeDeJeu(mode);
            game.reinitialiser();
            game.setScoreEnregistre(false);
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
            game.setScoreEnregistre(false);
            session.setAttribute("flashMessage", "✅ Configuration personnalisée appliquée !");
        } else {
            session.setAttribute("flashMessage", "❌ Erreur config : " + validation);
        }

        return "redirect:/placement";
    }

    private String genererCleSchema(JeuPlacement game) {
        TreeMap<String, Integer> tri = new TreeMap<>(game.getConfigurationRequise());
        return game.getModeDeJeu() + "|" + tri.toString();
    }

    private Optional<Utilisateur> recupererUtilisateurCourant(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return utilisateurRepository.findByEmail(auth.getName());
    }

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
        String pseudo = (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken))
                ? auth.getName() : "Invité";
        model.addAttribute("pseudo", pseudo);
    }

    private void traiterVictoireEtEnregistrerScore(JeuPlacement game, HttpSession session, Authentication auth) {
        if (game.isScoreEnregistre()) return;

        Optional<Utilisateur> userOpt = recupererUtilisateurCourant(auth);

        // --- UTILISATION DU CALCUL DU MODÈLE ---
        int scoreFinal = game.getScoreCourant();

        if (userOpt.isEmpty()) {
            session.setAttribute("flashMessage", "🏆 Gagné ! " + scoreFinal + " pts (Connectez-vous pour sauvegarder)");
            game.reinitialiser();
            game.setScoreEnregistre(false);
            return;
        }

        Utilisateur user = userOpt.get();
        String cleSchema = genererCleSchema(game);
        boolean dejaReussiGlobalement = scoreRepository.existsBySchemaKeyAndReussiTrue(cleSchema);

        // Bonus "Première fois" ajouté au score calculé par le jeu
        int bonus = (!dejaReussiGlobalement) ? 50 : 0;
        int total = scoreFinal + bonus;

        Score s = new Score();
        s.setUtilisateur(user);
        s.setMode("PLACEMENT");
        s.setSchemaKey(cleSchema);
        s.setPoints(total);
        s.setScore(total);
        s.setReussi(true);
        s.setFirstTime(!dejaReussiGlobalement);

        // On sauvegarde aussi le détail des erreurs pour les stats
        s.setErreurs(game.getErreurs());
        s.setPerfect(game.estTentativeParfaite());

        scoreRepository.save(s);

        game.reinitialiser();
        game.setScoreEnregistre(false);

        String msg = "🏆 Victoire ! +" + total + " pts";
        if (!dejaReussiGlobalement) msg += " (🌟 BONUS PIONNIER !)";
        session.setAttribute("flashMessage", msg);
    }

    private void preparerModele(Model model, JeuPlacement game) {
        model.addAttribute("board", game.getBoard());
        model.addAttribute("configRequise", game.getConfigurationRequise());
        model.addAttribute("compteActuel", game.getCompteActuelCalculated());
        model.addAttribute("gagne", game.estPuzzleResolu());

        // --- AJOUT : Données pour le panneau de score en temps réel ---
        model.addAttribute("scoreCourant", game.getScoreCourant());
        model.addAttribute("erreurs", game.getErreurs());
        model.addAttribute("tentativeParfaite", game.estTentativeParfaite());
    }

    // --- NOUVELLE MÉTHODE ADAPTÉE A TON REPOSITORY ---
    private void injecterClassement(Model model, JeuPlacement game) {
        // Utilise getClassementGlobal (retourne List<ClassementRow>)
        model.addAttribute("classementGlobal", scoreRepository.getClassementGlobal());

        String mode = game.getModeDeJeu();
        if (mode == null) mode = "8-dames";

        // Utilise getClassementParMode (retourne List<ClassementRow>)
        model.addAttribute("classementMode", scoreRepository.getClassementParMode(mode));
    }
}