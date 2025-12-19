package fr.univlorraine.pierreludmannchessmate.controller;

import fr.univlorraine.pierreludmannchessmate.JeuPuzzle;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String showPuzzle(@ModelAttribute("jeuPuzzle") JeuPuzzle game, Model model, Authentication auth, HttpSession session) {
        model.addAttribute("pseudo", auth != null ? auth.getName() : "Invité");

        // Vérification si plateau vide
        // SAFETY CHECK: If solutionMoves is null, we MUST load a puzzle
        // We access the field via reflection or simply rely on the existing logic if we add a getter,
        // but the easiest way is to modify your emptiness check:

        boolean plateauVide = true;
        // Check pieces...
        for(int i=0; i<8; i++) {
            for(int j=0; j<8; j++) {
                if(game.getBoard()[i][j] != null) { plateauVide = false; break; }
            }
        }

        // FORCE LOAD if board is empty OR if the game state is invalid (solutionMoves is null)
        // Since we can't easily check solutionMoves from outside without a getter,
        // let's rely on the fix in JeuPuzzle.java to prevent the crash,
        // and ensure we load if the board is empty.

        if (plateauVide) {
            chargerPuzzleSelonDifficulte(game);
        }

        Object msg = session.getAttribute("flashMessage");
        if (msg != null) {
            model.addAttribute("message", msg); // On l'envoie à la vue
            session.removeAttribute("flashMessage"); // On le supprime de la session
        }

        // Orientation
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

    // --- Actions existantes ---
    @PostMapping("/move")
    public String handleMove(@RequestParam int departX, @RequestParam int departY,
                             @RequestParam int arriveeX, @RequestParam int arriveeY,
                             @ModelAttribute("jeuPuzzle") JeuPuzzle game,
                             HttpSession session) {
        // Attention à l'ordre X/Y selon votre logique précédente
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

    // --- Endpoint : Changement de difficulté ---
    @PostMapping("/changeMode")
    public String changeMode(@RequestParam("difficulte") String difficulte,
                             @ModelAttribute("jeuPuzzle") JeuPuzzle game,
                             HttpSession session) {
        game.setDifficulte(difficulte);

        // On tente de charger. Si ça échoue, on prévient l'utilisateur.
        boolean succes = chargerPuzzleSelonDifficulte(game);

        if (!succes) {
            session.setAttribute("flashMessage", "⚠️ Aucun puzzle trouvé...");
        } else {
            session.setAttribute("flashMessage", "Puzzle chargé !");
        }
        return "redirect:/puzzle";
    }

    // --- Endpoint : Reset / Nouveau Puzzle ---
    @PostMapping("/reset")
    public String resetPuzzle(@ModelAttribute("jeuPuzzle") JeuPuzzle game,
                              RedirectAttributes redirectAttributes) {

        boolean succes = chargerPuzzleSelonDifficulte(game);

        if (!succes) {
            game.viderPlateau();
            redirectAttributes.addFlashAttribute("message", "⚠️ Impossible de charger un puzzle.");
        }
        return "redirect:/puzzle";
    }

    // --- LOGIQUE DE CHARGEMENT STRICTE ---
    // Retourne TRUE si un puzzle a été trouvé et chargé, FALSE sinon.
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

            // 1. On récupère l'ID du puzzle actuel pour ne pas le rejouer tout de suite
            String currentId = game.getPuzzleId();

            for (String line : lines) {
                try {
                    String[] tokens = line.split(",");
                    if (tokens.length < 3) continue;

                    String id = tokens[0].trim(); // L'ID est la première colonne

                    // NOUVEAU : Si c'est le même ID que celui en cours, on l'ignore direct
                    if (id.equals(currentId)) {
                        continue;
                    }

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

            // --- Si la liste est vide ---
            // Cela veut dire qu'il n'y a plus AUCUN puzzle "nouveau" disponible.
            if (candidats.isEmpty()) {
                System.out.println("Aucun nouveau puzzle trouvé pour diff=" + diff);
                return false;
            }

            // Sélection
            Random rand = new Random();
            String chosenLine = candidats.get(rand.nextInt(candidats.size()));

            String[] tokens = chosenLine.split(",");

            // NOUVEAU : On enregistre l'ID du nouveau puzzle
            game.setPuzzleId(tokens[0]);

            JSONObject json = new JSONObject();
            json.put("fen", tokens[1]);
            json.put("moves", tokens[2]);

            game.dechiffre_pb(json);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @PostMapping("/clear")
    public String clearBoard(@ModelAttribute("jeuPuzzle") JeuPuzzle game,
                             RedirectAttributes redirectAttributes) {
        game.viderPlateau();
        redirectAttributes.addFlashAttribute("message", "Plateau vidé.");
        return "redirect:/puzzle";
    }
}