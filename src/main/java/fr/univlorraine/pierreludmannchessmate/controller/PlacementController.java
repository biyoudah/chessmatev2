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
import org.springframework.web.bind.support.SessionStatus;

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

        // Nettoyage message flash
        Object msg = session.getAttribute("flashMessage");
        if (msg != null) {
            model.addAttribute("message", msg);
            session.removeAttribute("flashMessage");
        }

        preparerModele(model, game);
        return "placement"; // Assure-toi que ton fichier HTML s'appelle placement.html
    }

    @PostMapping("/action")
    public String action(@RequestParam int x, @RequestParam int y,
                         @RequestParam(required = false) String type,
                         @ModelAttribute("jeuPlacement") JeuPlacement game,
                         HttpSession session, Authentication auth) {

        if (game.getPieceObject(x, y) == null) {
            // Placement
            if (type != null && !type.isEmpty()) {
                String res = game.placerPieceJoueur(x, y, type, true);
                if (!"OK".equals(res)) {
                    session.setAttribute("flashMessage", "INVALID".equals(res) ? "⚠️ Case menacée !" : "❌ Case occupée !");
                }
            }
        } else {
            // Gomme
            game.retirerPiece(x, y);
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

    private void traiterVictoire(JeuPlacement game, HttpSession session, Authentication auth) {
        if (game.isScoreEnregistre()) return;

        int baseScore = game.calculerScoreFinalSansBonus();
        Optional<Utilisateur> userOpt = recupererUtilisateurCourant(auth);

        if (userOpt.isEmpty()) {
            session.setAttribute("flashMessage", "🏆 Réussi ! " + baseScore + " pts (Connectez-vous pour enregistrer)");
            game.reinitialiser();
            return;
        }

        Utilisateur user = userOpt.get();
        String cleSchema = game.getModeDeJeu() + "|" + new TreeMap<>(game.getConfigurationRequise());
        boolean premiereFois = !scoreRepository.existsByUtilisateurAndSchemaKey(user, cleSchema);

        int bonus = premiereFois ? Math.max(5, (int) Math.round(baseScore * 0.3)) : 0;
        int total = baseScore + bonus;

        Score s = new Score();
        s.setUtilisateur(user);
        s.setMode("PLACEMENT");
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
    }

    private void preparerModele(Model model, JeuPlacement game) {
        model.addAttribute("board", game.getBoard());
        model.addAttribute("configRequise", game.getConfigurationRequise());
        model.addAttribute("compteActuel", game.getCompteActuelCalculated());
        model.addAttribute("scoreCourant", game.getScoreCourant());
        model.addAttribute("erreurs", game.getErreurs());
        model.addAttribute("gagne", game.estPuzzleResolu());
        model.addAttribute("classementGlobal", scoreRepository.getClassementGlobal());
        model.addAttribute("classementMode", scoreRepository.getClassementParMode(game.getModeDeJeu()));
    }

    private void injecterInfosUtilisateur(Model model, Authentication auth) {
        boolean estConnecte = auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
        model.addAttribute("isLoggedIn", estConnecte);
        model.addAttribute("pseudo", estConnecte ? auth.getName() : "Invité");
    }

    private Optional<Utilisateur> recupererUtilisateurCourant(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) return Optional.empty();
        return utilisateurRepository.findByEmail(auth.getName());
    }
}