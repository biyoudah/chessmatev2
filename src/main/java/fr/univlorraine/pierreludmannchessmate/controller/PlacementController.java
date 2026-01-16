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

/**
 * Contrôleur du mode « Placement ».
 * <p>
 * Permet de placer/retirer des pièces pour résoudre des défis de composition
 * (ex: 8 dames) avec validation métier et gestion de feedback utilisateur.
 * Les informations de session (pseudo, statut) sont injectées pour la vue.
 */
@Controller
@RequestMapping("/placement")
@SessionAttributes("jeuPlacement")
public class PlacementController {

    private final UtilisateurRepository utilisateurRepository;
    private final ScoreRepository scoreRepository;

    /**
     * Constructeur avec injection des dépôts requis.
     * @param utilisateurRepository accès utilisateur pour récupérer le pseudo
     * @param scoreRepository accès aux scores pour l'enregistrement des résultats
     */
    public PlacementController(UtilisateurRepository utilisateurRepository, ScoreRepository scoreRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.scoreRepository = scoreRepository;
    }

    /**
     * Initialise l'instance de jeu en session si nécessaire.
     * @return une instance de {@link JeuPlacement}
     */
    @ModelAttribute("jeuPlacement")
    public JeuPlacement initPlacement() {
        return new JeuPlacement();
    }

    /**
     * Affiche la page du mode Placement et prépare les messages flash et le modèle.
     *
     * @param game jeu en session
     * @param model modèle de la vue
     * @param auth authentification courante
     * @param session session HTTP (messages flash, chronométrage)
     * @return le nom de la vue "placement"
     */
    @GetMapping
    public String afficher(@ModelAttribute("jeuPlacement") JeuPlacement game, Model model, Authentication auth, HttpSession session) {
        injecterInfosUtilisateur(model, auth);

        // RÉCUPÉRATION DE TOUS LES ÉLÉMENTS DE SESSION
        String[] sessionAttrs = {"flashMessage", "flashDetail", "flashType", "flashPerfect"};

        for (String attr : sessionAttrs) {
            Object val = session.getAttribute(attr);
            if (val != null) {
                // On transforme "flashPerfect" en "showPerfectMessage" pour le HTML
                if (attr.equals("flashPerfect")) {
                    model.addAttribute("showPerfectMessage", true);
                } else {
                    // On enlève le préfixe "flash" pour simplifier le HTML (ex: flashDetail -> detail)
                    model.addAttribute(attr.replace("flash", "").toLowerCase(), val);
                }
                session.removeAttribute(attr);
            }
        }

        if (session.getAttribute("startTime") == null) {
            session.setAttribute("startTime", System.currentTimeMillis());
        }

        preparerModele(model, game, auth);
        return "placement";
    }

    /**
     * Action principale de placement/retrait de pièce selon l'état de la case.
     * Déclenche la validation du puzzle et la gestion des messages utilisateur.
     *
     * @param x colonne cliquée (0..7)
     * @param y ligne cliquée (0..7)
     * @param type type de pièce à placer (Roi, Dame, Tour, Fou, Cavalier, Pion)
     * @param isWhite couleur de la pièce
     * @param game instance de jeu en session
     * @param session session HTTP pour les messages flash et le temps
     * @param auth authentification courante
     * @return redirection vers "/placement"
     */
    @PostMapping("/action")
    public String action(@RequestParam int x, @RequestParam int y,
                         @RequestParam(required = false) String type,
                         @RequestParam(required = false, defaultValue = "true") boolean isWhite,
                         @ModelAttribute("jeuPlacement") JeuPlacement game,
                         HttpSession session, Authentication auth) {

        if (game.getPieceObject(x, y) == null) {
            if (type != null && !type.isEmpty()) {
                String res = game.placerPieceJoueur(x, y, type, isWhite);

                if (res.startsWith("OK")) {
                    session.setAttribute("flashType", "place");
                } else {
                    String[] parts = res.split(":");
                    String mainMsg = parts[0].equals("INVALID") ? "⚠️ Coup illégal" : "❌ Case occupée";
                    String detailMsg = parts.length > 1 ? parts[1] : "";

                    session.setAttribute("flashMessage", mainMsg);
                    session.setAttribute("flashDetail", detailMsg);
                    session.setAttribute("flashType", "error");
                }
            }
        } else {
            game.retirerPiece(x, y);
            session.setAttribute("flashType", "remove");
        }

        if (game.estPuzzleResolu()) {
            traiterVictoire(game, session, auth);
        }

        return "redirect:/placement";
    }

    /**
     * Réinitialise le plateau et l'état du jeu de placement.
     * @param game jeu en session
     * @return redirection vers "/placement"
     */
    @PostMapping("/reset")
    public String reset(@ModelAttribute("jeuPlacement") JeuPlacement game) {
        game.reinitialiser();
        return "redirect:/placement";
    }

    /**
     * Change la configuration du défi (ex: 8 dames, 8 tours...).
     * @param modeDeJeu libellé du mode
     * @param game jeu en session
     * @return redirection vers "/placement"
     */
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

        boolean isPerfect = game.estTentativeParfaite();
        Optional<Utilisateur> userOpt = recupererUtilisateurCourant(auth);



        if (userOpt.isEmpty()) {
            session.setAttribute("flashMessage", "🏆 Réussi ! (Connectez-vous pour gagner des points)");
            session.setAttribute("flashType", "victory");
        } else {
            Utilisateur user = userOpt.get();
            String cleSchema = game.getModeDeJeu() + "|" + new TreeMap<>(game.getConfigurationRequise()).toString();

            boolean dejaResolu = scoreRepository.existsByUtilisateurAndSchemaKey(user, cleSchema);

            if (dejaResolu) {
                session.setAttribute("flashMessage", "Déjà résolu ! (Points déjà obtenus)");
                session.setAttribute("flashType", "victory");
            } else {
                int baseScore = game.calculerScoreFinalSansBonus();
                int bonus = Math.max(5, (int) Math.round(baseScore * 0.3));
                int total = baseScore + bonus;

                Score s = new Score();
                s.setUtilisateur(user);
                s.setMode("PLACEMENT");
                s.setSchemaKey(cleSchema);
                s.setPoints(total);
                s.setScore(total);
                s.setPerfect(isPerfect);
                s.setFirstTime(true);
                s.setReussi(true);

                scoreRepository.save(s);
                Long startTime = (Long) session.getAttribute("startTime");
                // Dans PlacementController.java -> traiterVictoire
                if (startTime != null) {
                    long secondesEcoulées = (System.currentTimeMillis() - startTime) / 1000;
                    utilisateurRepository.ajouterTempsDeJeu(user.getId(), secondesEcoulées);
                    session.setAttribute("startTime", System.currentTimeMillis());
                }

                session.setAttribute("flashMessage", "🏆 Première réussite ! +" + total + " pts");
                session.setAttribute("flashType", "victory");

            }
        }

        if (isPerfect) {
            session.setAttribute("flashPerfect", true);
        }

        game.setScoreEnregistre(true);
        game.reinitialiser();
    }

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
        long totalResol = scoreRepository.countByUtilisateurAndReussiTrue(user);
        long perfects = scoreRepository.countByUtilisateurAndPerfectTrue(user);
        long damesResol = scoreRepository.countByUtilisateurAndModeAndReussiTrue(user, "8-dames");
        long persoResol = scoreRepository.countByUtilisateurAndModeAndReussiTrue(user, "custom");
        boolean aJouePlusDuneHeure = user.getTempsTotalDeJeu() >= 3600;

        trophees.put("MaitreDesDames", damesResol >= 6);
        trophees.put("RoiDuPuzzle", totalResol >= 10);
        trophees.put("RoiDuPerfect", perfects >= 5);
        trophees.put("CavalierDuTemps", aJouePlusDuneHeure);
        trophees.put("FouDuPersonnalise", persoResol >= 7);
        trophees.put("ChessMate", perfects >=10 && persoResol >=8 && damesResol >= 6 & totalResol >20);

        return trophees;
    }

    private void injecterInfosUtilisateur(Model model, Authentication auth) {
        boolean estConnecte = auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
        model.addAttribute("isLoggedIn", estConnecte);
        if (estConnecte) {
            String email = auth.getName();

            String pseudo = utilisateurRepository.findByEmail(email)
                    .map(Utilisateur::getPseudo)
                    .orElse("Joueur");
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