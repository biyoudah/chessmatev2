package fr.univlorraine.pierreludmannchessmate.controller;

import fr.univlorraine.pierreludmannchessmate.logic.JeuPlacement;
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

import java.util.*;

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
    public String afficher(@ModelAttribute("jeuPlacement") JeuPlacement game, Model model, Authentication auth, HttpSession session) {
        injecterInfosUtilisateur(model, auth);

        // Dans PlacementController.java, méthode afficher()
        Object msg = session.getAttribute("flashMessage");
        if (msg != null) {
            model.addAttribute("message", msg);
            session.removeAttribute("flashMessage");
        }

        Object perfect = session.getAttribute("flashPerfect");
        if (perfect != null) {
            model.addAttribute("showPerfectMessage", true);
            session.removeAttribute("flashPerfect");
        }

        preparerModele(model, game, auth);
        return "placement";
    }

    @PostMapping("/action")
    public String action(@RequestParam int x, @RequestParam int y,
                         @RequestParam(required = false) String type,
                         @RequestParam(required = false, defaultValue = "true") boolean isWhite,
                         @ModelAttribute("jeuPlacement") JeuPlacement game,
                         HttpSession session, Authentication auth) {

        if (game.getPieceObject(x, y) == null) {
            if (type != null && !type.isEmpty()) {
                String res = game.placerPieceJoueur(x, y, type, isWhite);
                if (!"OK".equals(res)) {
                    session.setAttribute("flashMessage", "INVALID".equals(res) ? "⚠️ Case menacée !" : "❌ Case occupée !");
                    session.setAttribute("flashType", "error"); // Pour le son
                } else {
                    session.setAttribute("flashType", "place"); // Pour le son
                }
            }
        } else {
            game.retirerPiece(x, y);
            session.setAttribute("flashType", "remove"); // Pour le son
        }

        if (game.estPuzzleResolu()) {
            traiterVictoire(game, session, auth);
        }

        return "redirect:/placement";
    }

    @PostMapping("/reset")
    public String reset(@ModelAttribute("jeuPlacement") JeuPlacement game) {
        game.reinitialiser();
        return "redirect:/placement";
    }

    @PostMapping("/changeMode")
    public String changeMode(@RequestParam String modeDeJeu, @ModelAttribute("jeuPlacement") JeuPlacement game) {
        Map<String, Integer> config = new HashMap<>();
        switch (modeDeJeu) {
            case "8-dames" -> config.put("Dame", 8);
            case "8-tours" -> config.put("Tour", 8);
            case "14-fous" -> config.put("Fou", 14);
            case "16-rois" -> config.put("Roi", 16);
            default -> config.put("Dame", 8);
        }
        game.setConfigurationRequise(config);
        game.setModeDeJeu(modeDeJeu);
        game.reinitialiser();
        return "redirect:/placement";
    }

    @PostMapping("/customConfig")
    public String customConfig(@RequestParam Map<String, String> params, @ModelAttribute("jeuPlacement") JeuPlacement game, HttpSession session) {
        Map<String, Integer> config = new HashMap<>();
        try {
            if(params.get("c_dame") != null) config.put("Dame", Integer.parseInt(params.get("c_dame")));
            if(params.get("c_tour") != null) config.put("Tour", Integer.parseInt(params.get("c_tour")));
            if(params.get("c_fou") != null) config.put("Fou", Integer.parseInt(params.get("c_fou")));
            if(params.get("c_cavalier") != null) config.put("Cavalier", Integer.parseInt(params.get("c_cavalier")));
            if(params.get("c_roi") != null) config.put("Roi", Integer.parseInt(params.get("c_roi")));
        } catch (NumberFormatException e) {
            session.setAttribute("flashMessage", "❌ Erreur de format dans la configuration.");
            return "redirect:/placement";
        }

        String validation = game.validerConfiguration(config);
        if (!"OK".equals(validation)) {
            session.setAttribute("flashMessage", "⚠️ " + validation);
            return "redirect:/placement";
        }

        game.setConfigurationRequise(config);
        game.setModeDeJeu("custom");
        game.reinitialiser();
        return "redirect:/placement";
    }

    private void traiterVictoire(JeuPlacement game, HttpSession session, Authentication auth) {
        if (game.isScoreEnregistre()) return;

        // 1. IMPORTANT : On sauvegarde l'état Perfect AVANT de calculer ou reset
        boolean isPerfect = game.estTentativeParfaite();

        int baseScore = game.calculerScoreFinalSansBonus();
        Optional<Utilisateur> userOpt = recupererUtilisateurCourant(auth);

        String messageBase;

        if (userOpt.isEmpty()) {
            messageBase = "🏆 Réussi ! " + baseScore + " pts (Connectez-vous pour enregistrer)";
        } else {
            Utilisateur user = userOpt.get();
            String cleSchema = game.getModeDeJeu() + "|" + new TreeMap<>(game.getConfigurationRequise()).toString();
            boolean premiereFois = !scoreRepository.existsByUtilisateurAndSchemaKey(user, cleSchema);

            int bonus = premiereFois ? Math.max(5, (int) Math.round(baseScore * 0.3)) : 0;
            int total = baseScore + bonus;

            Score s = new Score();
            s.setUtilisateur(user);
            s.setMode(game.getModeDeJeu());
            s.setSchemaKey(cleSchema);
            s.setPoints(total);
            s.setScore(total);
            s.setBonusPremierSchemaAttribue(bonus);
            s.setErreurs(game.getErreurs());
            s.setErreursPlacement(game.getErreurs());
            s.setPerfect(isPerfect); // On utilise la variable capturée
            s.setFirstTime(premiereFois);
            s.setReussi(true);

            scoreRepository.save(s);

            messageBase = "🏆 Victoire ! +" + total + " pts" + (premiereFois ? " (Bonus inclus)" : "");
        }

        game.setScoreEnregistre(true);

        // 2. Si c'est un perfect, on ajoute le flag en session pour l'affichage HTML séparé
        if (isPerfect) {
            session.setAttribute("flashPerfect", true);
        }

        // 3. On définit le message flash classique
        session.setAttribute("flashMessage", messageBase);
        session.setAttribute("flashType", "victory");

        // 4. ET ENFIN on reset
        game.reinitialiser();
    }

    /*private void traiterVictoire(JeuPlacement game, HttpSession session, Authentication auth) {
        if (game.isScoreEnregistre()) return;

        int baseScore = game.calculerScoreFinalSansBonus();
        Optional<Utilisateur> userOpt = recupererUtilisateurCourant(auth);

        if (userOpt.isEmpty()) {
            session.setAttribute("flashMessage", "🏆 Réussi ! " + baseScore + " pts (Connectez-vous pour enregistrer)");
            session.setAttribute("flashType", "victory");

            game.reinitialiser();
            return;
        }

        Utilisateur user = userOpt.get();

        String cleSchema = game.getModeDeJeu() + "|" + new TreeMap<>(game.getConfigurationRequise()).toString();
        boolean premiereFois = !scoreRepository.existsByUtilisateurAndSchemaKey(user, cleSchema);

        int bonus = premiereFois ? Math.max(5, (int) Math.round(baseScore * 0.3)) : 0;
        int total = baseScore + bonus;

        Score s = new Score();
        s.setUtilisateur(user);
        s.setMode(game.getModeDeJeu());
        s.setSchemaKey(cleSchema);
        s.setPoints(total);
        s.setScore(total);
        s.setBonusPremierSchemaAttribue(bonus);
        s.setErreurs(game.getErreurs());
        s.setErreursPlacement(game.getErreurs());
        s.setPerfect(game.estTentativeParfaite());
        s.setFirstTime(premiereFois);
        s.setReussi(true);

        scoreRepository.save(s);
        game.setScoreEnregistre(true);
        game.reinitialiser();

        session.setAttribute("flashMessage", "🏆 Victoire ! +" + total + " pts" + (premiereFois ? " (Bonus inclus)" : ""));
        session.setAttribute("flashType", "victory");
    } */

    private void preparerModele(Model model, JeuPlacement game, Authentication auth) {
        model.addAttribute("board", game.getBoard());
        model.addAttribute("configRequise", game.getConfigurationRequise());
        model.addAttribute("compteActuel", game.getCompteActuelCalculated());
        model.addAttribute("scoreCourant", game.getScoreCourant());
        model.addAttribute("erreurs", game.getErreurs());
        model.addAttribute("gagne", game.estPuzzleResolu());
        model.addAttribute("tentativeParfaite", game.estTentativeParfaite());

        model.addAttribute("menaces", game.getMatriceMenaces());

        model.addAttribute("classementGlobal", scoreRepository.getClassementGlobal());
        model.addAttribute("classementMode", scoreRepository.getClassementParMode(game.getModeDeJeu()));

        Optional<Utilisateur> userOpt = recupererUtilisateurCourant(auth);
        if (userOpt.isPresent()) {
            Utilisateur user = userOpt.get();
            model.addAttribute("schemasCompletes", scoreRepository.findCompletedSchemaKeysByUser(user));
            model.addAttribute("trophees", calculerTrophees(user));
        } else {
            model.addAttribute("schemasCompletes", Collections.emptySet());
            model.addAttribute("trophees", Collections.emptyMap());
        }
    }

    private Map<String, Boolean> calculerTrophees(Utilisateur user) {
        Map<String, Boolean> trophees = new HashMap<>();
        long totalSolved = scoreRepository.countByUtilisateurAndReussiTrue(user);
        long perfects = scoreRepository.countByUtilisateurAndPerfectTrue(user);
        long damesSolved = scoreRepository.countByUtilisateurAndModeAndReussiTrue(user, "8-dames");

        trophees.put("MaitreDesDames", damesSolved >= 1); // A fini le mode 8 dames au moins une fois
        trophees.put("RoiDuPuzzle", totalSolved >= 10);   // A résolu 10 puzzles
        trophees.put("RoiDuFou", totalSolved >= 50);      // A résolu 50 puzzles (Exemple nom)
        trophees.put("RoiDuPerfect", perfects >= 5);      // A fait 5 perfects
        trophees.put("VoieDesTrophees", totalSolved >= 100); // Trophée ultime

        return trophees;
    }

    private void injecterInfosUtilisateur(Model model, Authentication auth) {
        boolean estConnecte = auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
        model.addAttribute("isLoggedIn", estConnecte);
        if (estConnecte) {
            String email = auth.getName(); // L'email de connexion

            // On va chercher le VRAI pseudo en base de données
            String pseudo = utilisateurRepository.findByEmail(email)
                    .map(Utilisateur::getPseudo)
                    .orElse("Joueur"); // Valeur par défaut si pseudo vide

            model.addAttribute("pseudo", pseudo);
        } else {
            model.addAttribute("pseudo", "Invité");
        }
    }

    private Optional<Utilisateur> recupererUtilisateurCourant(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) return Optional.empty();
        return utilisateurRepository.findByEmail(auth.getName());
    }
}