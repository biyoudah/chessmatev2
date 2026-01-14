package fr.univlorraine.pierreludmannchessmate.controller;

import fr.univlorraine.pierreludmannchessmate.logic.JeuPuzzle;
import fr.univlorraine.pierreludmannchessmate.model.Score;
import fr.univlorraine.pierreludmannchessmate.model.Utilisateur;
import fr.univlorraine.pierreludmannchessmate.repository.ScoreRepository;
import fr.univlorraine.pierreludmannchessmate.repository.UtilisateurRepository;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
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

@Controller
@RequestMapping("/puzzle")
@SessionAttributes("jeuPuzzle")
public class PuzzleController {

    private final UtilisateurRepository utilisateurRepository;
    private final ScoreRepository scoreRepository;

    public PuzzleController(UtilisateurRepository utilisateurRepository, ScoreRepository scoreRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.scoreRepository = scoreRepository;
    }

    @ModelAttribute("jeuPuzzle")
    public JeuPuzzle initPuzzle() {
        return new JeuPuzzle();
    }

    @GetMapping
    public String afficherPuzzle(@ModelAttribute("jeuPuzzle") JeuPuzzle game,
                                 Model model,
                                 Authentication auth,
                                 HttpSession session) {

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
    public String getHint(@ModelAttribute("jeuPuzzle") JeuPuzzle game, HttpSession session) {
        String coords = game.getCoupAide();
        if (coords != null) {
            session.setAttribute("hintCoords", coords);
            // Suppression du message "Indice"
        }
        return "redirect:/puzzle";
    }

    @PostMapping("/move")
    public String handleMove(@RequestParam int departX, @RequestParam int departY,
                             @RequestParam int arriveeX, @RequestParam int arriveeY,
                             @ModelAttribute("jeuPuzzle") JeuPuzzle game,
                             HttpSession session,
                             Authentication auth) {

        session.removeAttribute("hintCoords");
        String resultat = game.jouerCoupJoueur(departY, departX, arriveeY, arriveeX);

        if("GAGNE".equals(resultat)) {
            traiterVictoirePuzzle(game, session, auth);
        }

        return "redirect:/puzzle";
    }

    @PostMapping("/computer-move")
    public String computerMove(@ModelAttribute("jeuPuzzle") JeuPuzzle game) {
        game.reponseOrdinateur();
        return "redirect:/puzzle";
    }

    @PostMapping("/changeMode")
    public String changeMode(@RequestParam("difficulte") String difficulte,
                             @ModelAttribute("jeuPuzzle") JeuPuzzle game,
                             HttpSession session) {
        game.setDifficulte(difficulte);
        boolean succes = chargerPuzzleSelonDifficulte(game);

        if (!succes) {
            game.viderPlateau();
            // Suppression message erreur
        } else {
            game.setScoreEnregistre(false);
            // Suppression message confirmation
        }
        return "redirect:/puzzle";
    }

    @PostMapping("/reset")
    public String resetPuzzle(@ModelAttribute("jeuPuzzle") JeuPuzzle game, HttpSession session) {
        boolean succes = chargerPuzzleSelonDifficulte(game);
        if (succes) {
            game.setScoreEnregistre(false);
        } else {
            game.viderPlateau();
            // Suppression message erreur
        }
        return "redirect:/puzzle";
    }

    @PostMapping("/clear")
    public String clearBoard(@ModelAttribute("jeuPuzzle") JeuPuzzle game, HttpSession session) {
        game.viderPlateau();
        // Suppression message "Plateau vidé"
        return "redirect:/puzzle";
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
            // Suppression message "Déjà complété"
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
        // Suppression message "Bravo + points"
    }

    private Optional<Utilisateur> recupererUtilisateurCourant(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return utilisateurRepository.findByEmail(auth.getName());
    }

    private boolean chargerPuzzleSelonDifficulte(JeuPuzzle game) {
        try {
            ClassPathResource resource = new ClassPathResource("puzzle.csv");
            if (!resource.exists()) return false;
            List<String> lines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty() && !line.startsWith("PuzzleId")) lines.add(line);
                }
            }
            if (lines.isEmpty()) return false;
            String diff = game.getDifficulte();
            List<String> candidats = new ArrayList<>();
            String currentId = game.getPuzzleId();
            for (String line : lines) {
                try {
                    String[] tokens = line.split(",");
                    if (tokens.length < 3) continue;
                    if (tokens[0].trim().equals(currentId)) continue;
                    String moves = tokens[2].trim();
                    int nbMoves = moves.split("\\s+").length;
                    boolean match = switch (diff) {
                        case "1" -> nbMoves <= 2;
                        case "2" -> nbMoves > 2 && nbMoves <= 4;
                        case "3" -> nbMoves > 4;
                        default -> true;
                    };
                    if (match) candidats.add(line);
                } catch (Exception ignored) {}
            }
            if (candidats.isEmpty()) return false;
            Random rand = new Random();
            String chosenLine = candidats.get(rand.nextInt(candidats.size()));
            String[] tokens = chosenLine.split(",");
            game.setPuzzleId(tokens[0]);
            JSONObject json = new JSONObject();
            json.put("PuzzleId", tokens[0]);
            json.put("fen", tokens[1]);
            json.put("moves", tokens[2]);
            game.dechiffre_pb(json);
            return true;
        } catch (Exception e) { return false; }
    }

    @GetMapping("/admin/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminPanel(Model model) {
        return "adminPuzzle"; // Nouvelle vue HTML
    }

    @PostMapping("/admin/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String addPuzzleCsv(@RequestParam String puzzleId, @RequestParam String fen,
                               @RequestParam String moves, @RequestParam String rating,
                               @RequestParam String ratingConfidence, @RequestParam String popularity,
                               @RequestParam String nbPlays, @RequestParam String themes,
                               @RequestParam String gameUrl, @RequestParam String openingName) {

        // Formatage de la ligne CSV
        String newline = String.join(",", puzzleId, fen, moves, rating, ratingConfidence,
                popularity, nbPlays, themes, gameUrl, openingName);

        try {
            // Localisation du fichier dans resources (Note: En production, il vaut mieux viser un chemin externe)
            Path path = Paths.get(new ClassPathResource("puzzle.csv").getURI());
            Files.write(path, ("\n" + newline).getBytes(), StandardOpenOption.APPEND);
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/puzzle?error=csv";
        }

        return "redirect:/puzzle?success=added";
    }
}