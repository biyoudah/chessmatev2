

package fr.univlorraine.pierreludmannchessmate;

import fr.univlorraine.pierreludmannchessmate.model.Score;
import fr.univlorraine.pierreludmannchessmate.model.Utilisateur;
import fr.univlorraine.pierreludmannchessmate.repository.ScoreRepository;
import fr.univlorraine.pierreludmannchessmate.repository.UtilisateurRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@SessionAttributes("game")
public class ChessController {

    private final UtilisateurRepository utilisateurRepository;
    private final ScoreRepository scoreRepository;
    private final ChessApiService chessApiService;

    public ChessController(UtilisateurRepository utilisateurRepository, ScoreRepository scoreRepository, ChessApiService chessApiService) {
        this.utilisateurRepository = utilisateurRepository;
        this.scoreRepository = scoreRepository;
        this.chessApiService = chessApiService;
    }

    @ModelAttribute("game")
    public ChessGame initialiserJeu() {
        return new ChessGame();
    }

    @GetMapping("/")
    public String racine() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String accueil(Model modele, Authentication auth) {
        injecterInfosUtilisateur(modele, auth);
        return "home";
    }

    @GetMapping("/show")
    public String afficherPlateau(@ModelAttribute("game") ChessGame game, Model modele, Authentication auth) {
        actualiserModeleJeu(modele, game, auth);
        return "show";
    }

    @PostMapping("/place")
    public String placerPiece(@RequestParam int x, @RequestParam int y, @RequestParam String pieceType,
                              @RequestParam(defaultValue = "true") boolean estBlanc,
                              @ModelAttribute("game") ChessGame game, Model modele, Authentication auth) {
        String resultat = game.placerPiece(x, y, pieceType, estBlanc);
        if ("OCCUPEE".equals(resultat)) modele.addAttribute("message", "❌ Case déjà occupée !");
        else if ("INVALID".equals(resultat)) modele.addAttribute("message", "⚠️ Impossible : Case menacée !");

        if (game.estPuzzleResolu()) {
            traiterVictoireEtEnregistrerScore(game, modele, auth);
        }

        actualiserModeleJeu(modele, game, auth);
        return "show";
    }

    @PostMapping("/remove")
    public String retirerPiece(@RequestParam int x, @RequestParam int y,
                               @ModelAttribute("game") ChessGame game, Model modele, Authentication auth) {
        game.retirerPiece(x, y);
        actualiserModeleJeu(modele, game, auth);
        return "show";
    }

    @GetMapping("/puzzle")
    public String afficherPuzzle(@ModelAttribute("game") ChessGame game, Model modele, Authentication auth) {
        if (estPlateauVideOuDefaut(game)) {
            genererPuzzleAleatoire(game);
        }
        modele.addAttribute("traitAuBlanc", game.isTraitAuBlanc());
        actualiserModeleJeu(modele, game, auth);
        return "puzzle";
    }

    @PostMapping("/move")
    public String deplacerPiece(@RequestParam int departX, @RequestParam int departY,
                                @RequestParam int arriveeX, @RequestParam int arriveeY,
                                @ModelAttribute("game") ChessGame game, Model modele, Authentication auth) {
        String resultat = tenterDeplacerPiece(game, departX, departY, arriveeX, arriveeY);
        if ("OK".equals(resultat)) {
            modele.addAttribute("message", "Coup validé !");
            game.setTraitAuBlanc(!game.isTraitAuBlanc());
            if (game.estPuzzleResolu()) {
                traiterVictoireEtEnregistrerScore(game, modele, auth);
            }
        } else {
            modele.addAttribute("message", "⚠️ " + resultat);
        }
        modele.addAttribute("traitAuBlanc", game.isTraitAuBlanc());
        actualiserModeleJeu(modele, game, auth);
        return "puzzle";
    }

    @PostMapping("/api/puzzle")
    public String chargerNouveauPuzzleApi(@ModelAttribute("game") ChessGame game, Model modele, Authentication auth) {
        genererPuzzleAleatoire(game);
        modele.addAttribute("message", "✨ Nouveau puzzle chargé !");
        actualiserModeleJeu(modele, game, auth);
        return "puzzle";
    }

    @PostMapping("/reset")
    public String reinitialiserJeu(@ModelAttribute("game") ChessGame game) {
        game.reinitialiser();
        return "redirect:/show";
    }

    @PostMapping("/changeMode")
    public String changerModeDeJeu(@RequestParam String modeDeJeu, @ModelAttribute("game") ChessGame game, Model modele, Authentication auth) {
        if (!"custom".equals(modeDeJeu)) {
            configurerRegles(game, modeDeJeu);
            game.setModeDeJeu(modeDeJeu);
            game.reinitialiser();
        } else {
            game.setModeDeJeu("custom");
        }
        actualiserModeleJeu(modele, game, auth);
        return "show";
    }

    private void actualiserModeleJeu(Model modele, ChessGame game, Authentication auth) {
        injecterInfosUtilisateur(modele, auth);
        modele.addAttribute("board", game.getBoard());
        modele.addAttribute("configRequise", game.getConfigurationRequise());
        modele.addAttribute("compteActuel", game.getCompteActuel());
        modele.addAttribute("gagne", game.estPuzzleResolu());
        modele.addAttribute("scoreCourant", game.getScoreCourant());
        modele.addAttribute("erreurs", game.getErreurs());
        modele.addAttribute("classementGlobal", scoreRepository.getClassementGlobal());
        modele.addAttribute("classementMode", scoreRepository.getClassementParMode(game.getModeDeJeu()));
    }

    private void traiterVictoireEtEnregistrerScore(ChessGame game, Model modele, Authentication auth) {
        if (game.estScoreDejaEnregistre()) return;

        Optional<Utilisateur> userOpt = recupererUtilisateurCourant(auth);
        if (userOpt.isEmpty()) return;

        Utilisateur user = userOpt.get();
        String cleSchema = genererCleSchema(game);
        // Première fois désormais évaluée au niveau de la base (tous utilisateurs confondus)
        boolean premiereFois = !scoreRepository.existsBySchemaKeyAndReussiTrue(cleSchema);

        int base = game.calculerScoreFinalSansBonus();

        int bonus = premiereFois ? Math.max(5, (int) Math.round(base * 0.3)) : 0;
        int total = base + bonus;

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
        s.setTempsResolution(0); // À remplacer par game.getTemps() si disponible

        scoreRepository.save(s);
        game.marquerScoreEnregistre();

        String msg = "🏆 Configuration réussie ! +" + total + " pts" + (premiereFois ? " (Bonus nouveau schéma incl.)" : "");
        modele.addAttribute("message", msg);
        game.reinitialiser();

    }

    private Optional<Utilisateur> recupererUtilisateurCourant(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) return Optional.empty();
        return utilisateurRepository.findByEmail(auth.getName());
    }

    private String genererCleSchema(ChessGame game) {
        TreeMap<String, Integer> tri = new TreeMap<>(game.getConfigurationRequise());
        return game.getModeDeJeu() + "|" + tri.toString();
    }

    private void genererPuzzleAleatoire(ChessGame game) {
        game.chargerFen(chessApiService.getRandomPuzzleFen());
    }

    private boolean estPlateauVideOuDefaut(ChessGame game) {
        return game.getBoard()[0][4] == null || game.getBoard()[0][4].isEmpty();
    }


    private void injecterInfosUtilisateur(Model modele, Authentication auth) {
        boolean estConnecte = auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);

        modele.addAttribute("isLoggedIn", estConnecte);

        if (!estConnecte) {
            modele.addAttribute("pseudo", "Invité");
            return;
        }

        String email = auth.getName(); // ce que Spring Security connaît comme username
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElse(null);

        String pseudo = (utilisateur != null) ? utilisateur.getPseudo() : email;
        modele.addAttribute("pseudo", pseudo);
    }

    private void configurerRegles(ChessGame game, String mode) {
        Map<String, Integer> config = new HashMap<>();
        switch (mode) {
            case "8-dames" -> config.put("Dame", 8);
            case "8-tours" -> config.put("Tour", 8);
            case "14-fous" -> config.put("Fou", 14);
            case "16-rois" -> config.put("Roi", 16);
            default -> config.put("Dame", 8);
        }
        game.setConfigurationRequise(config);
    }

    private String tenterDeplacerPiece(ChessGame game, int dX, int dY, int aX, int aY) {
        Piece piece = game.recupererObjetPiece(dX, dY);
        if (piece == null) return "Case vide !";
        if (!piece.deplacementValide(dX, dY, aX, aY)) return "Mouvement impossible !";
        if (!game.cheminLibre(dX, dY, aX, aY)) return "Chemin obstrué !";

        game.retirerPiece(dX, dY);
        game.placerPiece(aX, aY, piece.getClass().getSimpleName(), piece.estBlanc());
        return "OK";
    }


}