package fr.univlorraine.pierreludmannchessmate;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

/**
 * Service gérant la communication avec les APIs d'échecs externes.
 * <p>
 * Ce service est responsable de la récupération de puzzles ou de positions
 * d'échecs (format FEN) depuis des services tiers (ex: Chess.com).
 */
@Service
public class ChessApiService {

    /**
     * Récupère une position de puzzle aléatoire depuis l'API publique de Chess.com.
     *
     * @return Une chaîne de caractères au format FEN représentant la position du puzzle.
     * En cas d'erreur de connexion, une position FEN par défaut (début de partie) est renvoyée.
     */
    public String getRandomPuzzleFen() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://api.chess.com/pub/puzzle/random";
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("fen")) {
                return (String) response.get("fen");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Fallback si l'API échoue : position de départ standard
        return "r1bqkbnr/pppp1ppp/2n5/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq - 2 3";
    }
}