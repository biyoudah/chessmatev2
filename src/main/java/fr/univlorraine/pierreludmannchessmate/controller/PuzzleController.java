package fr.univlorraine.pierreludmannchessmate.controller;

import fr.univlorraine.pierreludmannchessmate.JeuPuzzle;
import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/puzzle")
@SessionAttributes("jeuPuzzle")
public class PuzzleController {

    @ModelAttribute("jeuPuzzle")
    public JeuPuzzle initPuzzle() {
        System.out.println("--- NOUVELLE SESSION PUZZLE INITIALISÉE ---");
        return new JeuPuzzle();
    }

    @GetMapping
    public String showPuzzle(@ModelAttribute("jeuPuzzle") JeuPuzzle game,
                             Model model,
                             Authentication auth) {
        injecterInfosUtilisateur(model, auth);

        // 1. Vérification robuste : Est-ce que le plateau contient des pièces ?
        boolean plateauVide = true;

        // On vérifie si au moins une pièce existe sur le plateau
        verification:
        for(int i=0; i<8; i++) {
            for(int j=0; j<8; j++) {
                // ATTENTION : On suppose ici que game.getPieceObject(i, j) renvoie null si vide
                // ou que game.getBoard()[i][j] n'est pas null mais sa toString est vide.
                // Adaptez selon votre classe AbstractChessGame.
                if(game.getBoard()[i][j] != null && !game.getBoard()[i][j].toString().isEmpty()) {
                    plateauVide = false;
                    break verification;
                }
            }
        }

        if (plateauVide) {
            System.out.println("Plateau vide détecté, chargement d'un puzzle...");
            chargerPuzzleAleatoire(game);
        }

        // --- LOGIQUE D'ORIENTATION ---

        List<Integer> rows;
        List<Integer> cols;

        if (game.isVueJoueurEstBlanc()) {
            // Vue fixe BLANCS en bas
            rows = IntStream.rangeClosed(0, 7).map(i -> 7 - i).boxed().toList();
            cols = IntStream.rangeClosed(0, 7).boxed().toList();
        } else {
            // Vue fixe NOIRS en bas
            rows = IntStream.rangeClosed(0, 7).boxed().toList();
            cols = IntStream.rangeClosed(0, 7).map(i -> 7 - i).boxed().toList();
        }

        model.addAttribute("rows", rows);
        model.addAttribute("cols", cols);

        model.addAttribute("board", game.getBoard());
        model.addAttribute("traitAuBlanc", game.isTraitAuBlanc());

        return "jeuPuzzle";
    }

    @PostMapping("/move")
    public String handleMove(@RequestParam int departX, @RequestParam int departY,
                             @RequestParam int arriveeX, @RequestParam int arriveeY,
                             @ModelAttribute("jeuPuzzle") JeuPuzzle game,
                             RedirectAttributes redirectAttributes) {

        // CORRECTION CRITIQUE : On inverse X et Y.
        // HTML envoie : departX (Ligne), departY (Colonne)
        // JeuPuzzle attend : Col, Ligne, Col, Ligne
        String resultat = game.jouerCoupJoueur(departY, departX, arriveeY, arriveeX);

        if("GAGNE".equals(resultat)) {
            redirectAttributes.addFlashAttribute("message", "✅ Bravo !");
        } else if("RATE".equals(resultat)) {
            redirectAttributes.addFlashAttribute("message", "❌ Mauvais coup !");
        }

        // Si le résultat est "RATE", le plateau ne bouge pas, c'est normal.
        // Si le résultat est "CONTINUE" ou "GAGNE", la pièce bouge.

        return "redirect:/puzzle";
    }

    @PostMapping("/computer-move")
    public String computerMove(@ModelAttribute("jeuPuzzle") JeuPuzzle game) {
        game.reponseOrdinateur();
        return "redirect:/puzzle";
    }

    @PostMapping("/reset")
    public String resetPuzzle(SessionStatus status) {
        status.setComplete();
        return "redirect:/puzzle";
    }

    private void injecterInfosUtilisateur(Model model, Authentication authentication) {
        boolean isConnected = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
        model.addAttribute("pseudo", isConnected ? authentication.getName() : "Invité");
    }

    private void chargerPuzzleAleatoire(JeuPuzzle game) {
        try {
            ClassPathResource resource = new ClassPathResource("puzzle.csv");

            if (!resource.exists()) {
                System.err.println("ERREUR CRITIQUE : Le fichier puzzle.csv est introuvable dans src/main/resources !");
                return;
            }

            List<String> lines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) lines.add(line);
            }

            if (lines.isEmpty()) {
                System.err.println("ERREUR : Le fichier puzzle.csv est vide !");
                return;
            }

            Random rand = new Random();
            // On ignore la première ligne (en-tête) si elle contient "PuzzleId"
            int start = lines.get(0).startsWith("PuzzleId") ? 1 : 0;
            int index = rand.nextInt(lines.size() - start) + start;

            String line = lines.get(index);
            System.out.println("Chargement du puzzle : " + line); // LOG pour vérifier

            String[] tokens = line.split(",");
            // Token 1 = FEN, Token 2 = Moves
            // Attention aux virgules dans le CSV, parfois le split simple échoue si les données contiennent des virgules
            if (tokens.length < 3) {
                System.err.println("Format CSV invalide pour la ligne : " + line);
                return;
            }

            JSONObject json = new JSONObject();
            json.put("fen", tokens[1]);
            json.put("moves", tokens[2]);

            game.dechiffre_pb(json);
            System.out.println("Puzzle chargé avec succès !");

        } catch (Exception e) {
            System.err.println("Exception lors du chargement du puzzle :");
            e.printStackTrace();
        }
    }
}