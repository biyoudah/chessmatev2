package fr.univlorraine.pierreludmannchessmate.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;

@Service
public class ChessApiService {

    private final RestTemplate restTemplate = new RestTemplate();

    private HttpEntity<String> getEntity() {
        HttpHeaders headers = new HttpHeaders();
        // Identification obligatoire pour Chess.com et fortement recommandée pour Lichess
        headers.set("User-Agent", "Chessmate-Univ-Lorraine (contact: ton-email@univ-lorraine.fr)");
        return new HttpEntity<>(headers);
    }

    public List<Map<String, Object>> getRecentArticles() {
        return List.of(
                Map.of("title", "Bienvenue sur Chessmate", "short", "Découvrez les nouvelles fonctionnalités.", "url", "#", "image", "img/ChessMate.png"),
                Map.of("title", "Tournoi de Noël", "short", "Les inscriptions sont ouvertes.", "url", "#", "image", "img/ChessMate.png")
        );
    }

    public List<Map<String, Object>> getLiveTournaments() {
        String url = "https://lichess.org/api/tournament";
        try {
            // Utilisation de exchange au lieu de getForObject pour envoyer les headers
            ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, getEntity(), Map.class);
            Map<String, Object> response = responseEntity.getBody();

            if (response != null && response.containsKey("started")) {
                List<Map<String, Object>> startedTournaments = (List<Map<String, Object>>) response.get("started");
                return startedTournaments.stream().limit(5).toList();
            }
        } catch (Exception e) {
            System.err.println("Erreur Lichess: " + e.getMessage());
        }
        return List.of();
    }
}