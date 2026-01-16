package fr.univlorraine.pierreludmannchessmate.controller;

import fr.univlorraine.pierreludmannchessmate.logic.JeuPuzzle;
import fr.univlorraine.pierreludmannchessmate.model.Puzzle;
import fr.univlorraine.pierreludmannchessmate.model.Score;
import fr.univlorraine.pierreludmannchessmate.model.Utilisateur;
import fr.univlorraine.pierreludmannchessmate.repository.PuzzleRepository;
import fr.univlorraine.pierreludmannchessmate.repository.ScoreRepository;
import fr.univlorraine.pierreludmannchessmate.repository.UtilisateurRepository;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity; // <--- IMPORT IMPORTANT
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Contrôleur du mode « Puzzle ».
 * <p>
 * Gère l'affichage du puzzle, le chargement selon la difficulté, les actions
 * de jeu (coup du joueur, réponse de l'ordinateur, indice), ainsi que la
 * persistance des scores et quelques mécanismes d'administration.
 */
@Controller
@RequestMapping("/puzzle")
@SessionAttributes("jeuPuzzle")
public class PuzzleController {

    private final UtilisateurRepository utilisateurRepository;
    private final ScoreRepository scoreRepository;
    private final PuzzleRepository puzzleRepository;


    /**
     * Constructeur avec injection des dépôts nécessaires.
     * @param utilisateurRepository accès aux utilisateurs pour les informations de session
     * @param scoreRepository accès aux classements et enregistrement des scores
     */
    public PuzzleController(UtilisateurRepository utilisateurRepository, ScoreRepository scoreRepository, PuzzleRepository puzzleRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.scoreRepository = scoreRepository;
        this.puzzleRepository = puzzleRepository;
    }

    /**
     * Crée l'instance de jeu associée à la session si absente.
     * @return une instance de {@link JeuPuzzle}
     */
    @ModelAttribute("jeuPuzzle")
    public JeuPuzzle initPuzzle() {
        return new JeuPuzzle();
    }

    /**
     * Affiche la page du mode Puzzle. Prépare le plateau (charge un puzzle si vide),
     * le contexte utilisateur et le classement.
     *
     * @param game jeu en session
     * @param model modèle Thymeleaf
     * @param auth authentification courante
     * @param session session HTTP pour gérer les messages flash/indice
     * @return le nom de la vue "puzzle"
     */
    @GetMapping
    public String afficherPuzzle(@ModelAttribute("jeuPuzzle") JeuPuzzle game,
                                 Model model,
                                 Authentication auth,
                                 HttpSession session) {

        String[] sessionAttrs = {"flashMessage", "flashDetail", "flashType"};

        for (String attr : sessionAttrs) {
            Object val = session.getAttribute(attr);
            if (val != null) {
                model.addAttribute(attr.replace("flash", "").toLowerCase(), val);
                session.removeAttribute(attr);
            }
        }

        boolean isLoggedIn = auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
        model.addAttribute("isLoggedIn", isLoggedIn);

        if (isLoggedIn) {
            String pseudo = recupererUtilisateurCourant(auth)
                    .map(Utilisateur::getPseudo)
                    .orElse("Joueur");
            model.addAttribute("pseudo", pseudo);
        } else {
            model.addAttribute("pseudo", "Invité");
        }

        Object hint = session.getAttribute("hintCoords");
        if (hint != null) {
            model.addAttribute("hintCoords", hint);
        }

        boolean plateauVide = true;
        for(int i=0; i<8; i++) {
            for(int j=0; j<8; j++) {
                if(game.getBoard()[i][j] != null) { plateauVide = false; break; }
            }
        }

        if (plateauVide) {
            chargerPuzzleSelonDifficulte(game);
            game.setScoreEnregistre(false);
        }

        List<Integer> rows = game.isVueJoueurEstBlanc()
                ? IntStream.rangeClosed(0, 7).map(i -> 7 - i).boxed().toList()
                : IntStream.rangeClosed(0, 7).boxed().toList();
        List<Integer> cols = game.isVueJoueurEstBlanc()
                ? IntStream.rangeClosed(0, 7).boxed().toList()
                : IntStream.rangeClosed(0, 7).map(i -> 7 - i).boxed().toList();

        model.addAttribute("rows", rows);
        model.addAttribute("cols", cols);
        model.addAttribute("board", game.getBoard());
        model.addAttribute("traitAuBlanc", game.isTraitAuBlanc());

        model.addAttribute("classementGlobal", scoreRepository.getClassementGlobal());
        model.addAttribute("classementTactique", scoreRepository.getClassementParMode("PUZZLE"));

        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);

        return "jeuPuzzle";
    }


    @PostMapping("/hint")
    @ResponseBody
    public ResponseEntity<Void> getHint(@ModelAttribute("jeuPuzzle") JeuPuzzle game, HttpSession session) {
        String coords = game.getCoupAide();
        if (coords != null) {
            session.setAttribute("hintCoords", coords);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/move")
    @ResponseBody
    public ResponseEntity<Void> handleMove(@RequestParam int departX, @RequestParam int departY,
                                           @RequestParam int arriveeX, @RequestParam int arriveeY,
                                           @ModelAttribute("jeuPuzzle") JeuPuzzle game,
                                           HttpSession session,
                                           Authentication auth) {

        session.removeAttribute("hintCoords");
        String resultat = game.jouerCoupJoueur(departY, departX, arriveeY, arriveeX);

        if ("GAGNE".equals(resultat)) {
            traiterVictoirePuzzle(game, session, auth);
            session.setAttribute("flashMessage", "Puzzle Résolu !");
            session.setAttribute("flashType", "victory");
            session.setAttribute("flashDetail", "Bien joué, vous avez trouvé le mat.");
        }
        else if ("RATE".equals(resultat) || "ECHEC".equals(resultat)) {
            session.setAttribute("flashMessage", "Mauvais coup !");
            session.setAttribute("flashType", "error");
            session.setAttribute("flashDetail", "Ce n'est pas la solution. Réessayez.");
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/computer-move")
    @ResponseBody
    public ResponseEntity<Void> computerMove(@ModelAttribute("jeuPuzzle") JeuPuzzle game) {
        game.reponseOrdinateur();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/changeMode")
    @ResponseBody
    public ResponseEntity<Void> changeMode(@RequestParam("difficulte") String difficulte,
                                           @ModelAttribute("jeuPuzzle") JeuPuzzle game,
                                           HttpSession session) {
        game.setDifficulte(difficulte);
        boolean succes = chargerPuzzleSelonDifficulte(game);

        if (!succes) {
            game.viderPlateau();
        } else {
            game.setScoreEnregistre(false);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset")
    @ResponseBody
    public ResponseEntity<Void> resetPuzzle(@ModelAttribute("jeuPuzzle") JeuPuzzle game, HttpSession session) {
        boolean succes = chargerPuzzleSelonDifficulte(game);
        if (succes) {
            game.setScoreEnregistre(false);
        } else {
            game.viderPlateau();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/clear")
    @ResponseBody
    public ResponseEntity<Void> clearBoard(@ModelAttribute("jeuPuzzle") JeuPuzzle game, HttpSession session) {
        game.viderPlateau();
        return ResponseEntity.ok().build();
    }


    private void traiterVictoirePuzzle(JeuPuzzle game, HttpSession session, Authentication auth) {
        if (game.isScoreEnregistre()) return;

        Optional<Utilisateur> userOpt = recupererUtilisateurCourant(auth);

        if (userOpt.isEmpty()) {
            game.setScoreEnregistre(true);
            return;
        }

        Utilisateur user = userOpt.get();
        int nouveauxPoints = switch (game.getDifficulte()) {
            case "1" -> 10;
            case "2" -> 25;
            case "3" -> 50;
            default  -> 10;
        };

        String schemaKey = "PUZZLE_" + game.getPuzzleId();
        boolean dejaReussi = scoreRepository.existsByUtilisateurAndSchemaKey(user, schemaKey);

        if (dejaReussi) {
            game.setScoreEnregistre(true);
            return;
        }

        Score s = new Score();
        s.setUtilisateur(user);
        s.setMode("PUZZLE");
        s.setSchemaKey(schemaKey);
        s.setPoints(nouveauxPoints);
        s.setScore(nouveauxPoints);
        s.setBonusPremierSchemaAttribue(0);
        s.setErreurs(0);
        s.setErreursPlacement(0);
        s.setTempsResolution(0);
        s.setFirstTime(false);
        s.setReussi(true);
        s.setPerfect(true);
        scoreRepository.save(s);

        game.setScoreEnregistre(true);
    }

    private Optional<Utilisateur> recupererUtilisateurCourant(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return utilisateurRepository.findByEmail(auth.getName());
    }

    private boolean chargerPuzzleSelonDifficulte(JeuPuzzle game) {
        int minMoves = 1;
        int maxMoves = 2;

        switch (game.getDifficulte()) {
            case "2" -> { minMoves = 3; maxMoves = 4; }
            case "3" -> { minMoves = 5; maxMoves = 20; }
        }

        // 1. On cherche l'entité Puzzle en base
        Optional<Puzzle> puzzleData = puzzleRepository.findRandomByMoveCount(minMoves, maxMoves);

        if (puzzleData.isPresent()) {
            Puzzle p = puzzleData.get();

            // 2. On prépare le JSON que votre méthode dechiffre_pb attend
            JSONObject json = new JSONObject();
            json.put("PuzzleId", p.getPuzzleId());
            json.put("fen", p.getFen());
            json.put("moves", p.getMoves());

            // 3. On "nourrit" le moteur de jeu avec ces données
            game.setPuzzleId(p.getPuzzleId());
            game.dechiffre_pb(json);

            return true;
        }

        return false;
    }

    @GetMapping("/admin/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminPanel(Model model) {
        return "adminPuzzle";
    }

    @PostMapping("/admin/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String addPuzzleCsv(@RequestParam String puzzleId, @RequestParam String fen,
                               @RequestParam String moves, @RequestParam Integer rating,
                               @RequestParam Integer ratingConfidence, @RequestParam Integer popularity,
                               @RequestParam Integer nbPlays, @RequestParam String themes,
                               @RequestParam String gameUrl, @RequestParam String openingName,
                               HttpSession session) {

        try {
            Puzzle newPuzzle = new Puzzle();
            newPuzzle.setPuzzleId(puzzleId);
            newPuzzle.setFen(fen);
            newPuzzle.setMoves(moves);
            newPuzzle.setRating(rating);

            puzzleRepository.save(newPuzzle);

            session.setAttribute("flashMessage", "Puzzle ajouté avec succès !");
            session.setAttribute("flashType", "victory");
            session.setAttribute("flashDetail", "Le puzzle " + puzzleId + " est désormais disponible en base de données.");

        } catch (Exception e) {
            session.setAttribute("flashMessage", "Erreur lors de l'ajout");
            session.setAttribute("flashType", "error");
            session.setAttribute("flashDetail", "Impossible d'enregistrer le puzzle : " + e.getMessage());
        }

        return "redirect:/puzzle";
    }
}