package fr.univlorraine.pierreludmannchessmate.controller;

import fr.univlorraine.pierreludmannchessmate.logic.JeuPuzzle;
import fr.univlorraine.pierreludmannchessmate.model.Score;
import fr.univlorraine.pierreludmannchessmate.model.Utilisateur;
import fr.univlorraine.pierreludmannchessmate.repository.ScoreRepository;
import fr.univlorraine.pierreludmannchessmate.repository.UtilisateurRepository;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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

        model.addAttribute("pseudo", auth != null ? auth.getName() : "Invité");

        Object msg = session.getAttribute("flashMessage");
        if (msg != null) {
            model.addAttribute("message", msg);
            session.removeAttribute("flashMessage");
        }

        Object hint = session.getAttribute("hintCoords");
        if (hint != null) {
            model.addAttribute("hintCoords", hint);
        }

        // Vérif plateau vide
        boolean plateauVide = true;
        for(int i=0; i<8; i++) {
            for(int j=0; j<8; j++) {
                if(game.getBoard()[i][j] != null) { plateauVide = false; break; }
            }
        }

        if (plateauVide) {
            chargerPuzzleSelonDifficulte(game);
            game.setScoreEnregistre(false); // Nouveau puzzle = score pas encore enregistré
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

        return "jeuPuzzle";
    }

    @PostMapping("/hint")
    public String getHint(@ModelAttribute("jeuPuzzle") JeuPuzzle game, HttpSession session) {
        String coords = game.getCoupAide();
        if (coords != null) {
            session.setAttribute("hintCoords", coords);
            session.setAttribute("flashMessage", "💡 Indice : Jouez la pièce en violet !");
        } else {
            session.setAttribute("flashMessage", "Pas d'indice disponible.");
        }
        return "redirect:/puzzle";
    }

    @PostMapping("/move")
    public String handleMove(@RequestParam int departX, @RequestParam int departY,
                             @RequestParam int arriveeX, @RequestParam int arriveeY,
                             @ModelAttribute("jeuPuzzle") JeuPuzzle game,
                             HttpSession session,
                             Authentication auth) { // Ajout Auth

        session.removeAttribute("hintCoords");

        String resultat = game.jouerCoupJoueur(departY, departX, arriveeY, arriveeX);

        if("GAGNE".equals(resultat)) {
            session.setAttribute("flashMessage", "✅ Bravo !");
            traiterVictoirePuzzle(game, session, auth); // SAUVEGARDE DU SCORE
        }
        else if("RATE".equals(resultat)) {
            session.setAttribute("flashMessage", "❌ Mauvais coup !");
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
            session.setAttribute("flashMessage", "⚠️ Aucun puzzle trouvé...");
        } else {
            game.setScoreEnregistre(false); // Reset score
            session.setAttribute("flashMessage", "Puzzle chargé (Niveau " + difficulte + ")");
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
            session.setAttribute("flashMessage", "⚠️ Impossible de charger un puzzle.");
        }
        return "redirect:/puzzle";
    }

    @PostMapping("/clear")
    public String clearBoard(@ModelAttribute("jeuPuzzle") JeuPuzzle game, HttpSession session) {
        game.viderPlateau();
        session.setAttribute("flashMessage", "Plateau vidé.");
        return "redirect:/puzzle";
    }

    // --------------------------------------------------------
    // LOGIQUE SCORE PUZZLE
    // --------------------------------------------------------
    private void traiterVictoirePuzzle(JeuPuzzle game, HttpSession session, Authentication auth) {
        if (game.isScoreEnregistre()) return;

        Optional<Utilisateur> userOpt = recupererUtilisateurCourant(auth);

        // Points fixes selon difficulte
        int pts = switch (game.getDifficulte()) {
            case "1" -> 10;
            case "2" -> 25;
            case "3" -> 50;
            default -> 10;
        };

        if (userOpt.isEmpty()) {
            game.setScoreEnregistre(true);
            // On peut ajouter le score au message flash si on veut
            return;
        }

        Utilisateur user = userOpt.get();
        // Clé unique : type de jeu + ID du puzzle CSV
        String schemaKey = "TACTIQUE_" + game.getPuzzleId();

        boolean dejaFait = scoreRepository.existsBySchemaKeyAndReussiTrue(schemaKey);
        int bonus = (!dejaFait) ? 5 : 0; // Petit bonus découverte
        int total = pts + bonus;

        Score s = new Score();
        s.setUtilisateur(user);
        s.setMode("TACTIQUE"); // ou "TACTIQUE_" + game.getDifficulte() pour trier par niveau
        s.setSchemaKey(schemaKey);
        s.setPoints(total);
        s.setScore(total);
        s.setReussi(true);
        s.setFirstTime(!dejaFait);

        scoreRepository.save(s);
        game.setScoreEnregistre(true);

        session.setAttribute("flashMessage", "✅ Bravo ! +" + total + " pts");
    }

    private Optional<Utilisateur> recupererUtilisateurCourant(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return utilisateurRepository.findByEmail(auth.getName());
    }

    // --- Chargement CSV (identique à votre code) ---
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
            json.put("fen", tokens[1]);
            json.put("moves", tokens[2]);
            game.dechiffre_pb(json);
            return true;
        } catch (Exception e) { return false; }
    }
}