package fr.univlorraine.pierreludmannchessmate.controller;

import fr.univlorraine.pierreludmannchessmate.JeuPuzzle;
import jakarta.servlet.http.HttpSession; // IMPORTANT
import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/puzzle")
@SessionAttributes("jeuPuzzle")
public class PuzzleController {

    @ModelAttribute("jeuPuzzle")
    public JeuPuzzle initPuzzle() {
        return new JeuPuzzle();
    }

    @GetMapping
    public String showPuzzle(@ModelAttribute("jeuPuzzle") JeuPuzzle game,
                             Model model,
                             Authentication auth,
                             HttpSession session) {

        model.addAttribute("pseudo", auth != null ? auth.getName() : "Invité");

        // --- 1. Gestion des messages Flash ---
        Object msg = session.getAttribute("flashMessage");
        if (msg != null) {
            model.addAttribute("message", msg);
            session.removeAttribute("flashMessage");
        }

        // --- 2. Gestion de l'Indice (HINT) ---
        // Si l'utilisateur a demandé un indice, on l'injecte dans le modèle
        Object hint = session.getAttribute("hintCoords");
        if (hint != null) {
            model.addAttribute("hintCoords", hint);
        }

        // --- 3. Chargement sécu ---
        boolean plateauVide = true;
        for(int i=0; i<8; i++) {
            for(int j=0; j<8; j++) {
                if(game.getBoard()[i][j] != null) { plateauVide = false; break; }
            }
        }

        if (plateauVide) {
            chargerPuzzleSelonDifficulte(game);
        }

        // --- 4. Affichage ---
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

    // --- NOUVELLE ACTION : AIDE ---
    @PostMapping("/hint")
    public String getHint(@ModelAttribute("jeuPuzzle") JeuPuzzle game, HttpSession session) {
        String coords = game.getCoupAide();

        if (coords != null) {
            session.setAttribute("hintCoords", coords);
            session.setAttribute("flashMessage", "💡 Indice : Jouez la pièce en violet !");
        } else {
            session.setAttribute("flashMessage", "Pas d'indice disponible (puzzle fini ou non chargé).");
        }
        return "redirect:/puzzle";
    }

    // --- Actions existantes (Corrigées avec HttpSession) ---

    @PostMapping("/move")
    public String handleMove(@RequestParam int departX, @RequestParam int departY,
                             @RequestParam int arriveeX, @RequestParam int arriveeY,
                             @ModelAttribute("jeuPuzzle") JeuPuzzle game,
                             HttpSession session) {

        session.removeAttribute("hintCoords");

        String resultat = game.jouerCoupJoueur(departY, departX, arriveeY, arriveeX);

        if("GAGNE".equals(resultat)) session.setAttribute("flashMessage", "✅ Bravo !");
        else if("RATE".equals(resultat)) session.setAttribute("flashMessage", "❌ Mauvais coup !");

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
            session.setAttribute("flashMessage", "Puzzle chargé (Niveau " + difficulte + ")");
        }
        return "redirect:/puzzle";
    }

    @PostMapping("/reset")
    public String resetPuzzle(@ModelAttribute("jeuPuzzle") JeuPuzzle game, HttpSession session) {
        boolean succes = chargerPuzzleSelonDifficulte(game);
        if (!succes) {
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

    // ... (Méthode chargerPuzzleSelonDifficulte inchangée, gardez la vôtre) ...
    // J'inclus juste la signature pour que le code compile si vous copiez-collez tout le fichier
    private boolean chargerPuzzleSelonDifficulte(JeuPuzzle game) {
        // ... VOTRE CODE EXISTANT DE CHARGEMENT ...
        // (Assurez-vous d'avoir remis le code avec la lecture du CSV ici)
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