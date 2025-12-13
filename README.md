# ♟️ ChessMate - Puzzle Solver

**ChessMate** est une application web interactive de résolution de puzzles d'échecs. Elle se concentre sur des problèmes de logique combinatoire et de placement (comme le problème des N-Dames), permettant aux utilisateurs de résoudre des défis classiques ou de créer leurs propres configurations.

![Aperçu de ChessMate](src/main/resources/static/img/ChessMateWithoutBackground.png)

## 🚀 Fonctionnalités

### 🧩 Modes de Jeu Variés
* **Les Classiques :**
  * **8 Dames :** Placer 8 dames sans qu'elles ne s'attaquent.
  * **8 Tours :** Le problème classique d'indépendance des tours.
  * **14 Fous :** Optimisation du placement des fous sur les diagonales.
  * **16 Rois :** Problème de pavage maximal avec des rois.
  * **🛠️ Mode Personnalisé :**
    * L'utilisateur définit ses propres règles (ex: "Je veux placer 4 Dames et 2 Tours").
    * Validation automatique de la faisabilité théorique par le serveur.

### 🎮 Expérience Utilisateur (UX)
* **Interface Réactive (AJAX) :** Toutes les actions (placer, retirer, changer de mode) se font sans recharger la page.
* **Feedback Instantané :**
  * Système de **notifications (Toasts)** pour alerter en cas de coup invalide ou de victoire.
  * Indicateurs visuels des objectifs (ex: "Dames : 3 / 8").
* **Validation des Règles :** Impossible de poser une pièce sur une case menacée (gestion des conflits en temps réel).
* **Sélection Persistante :** Permet de placer plusieurs pièces du même type à la suite.

### 🔐 Sécurité & Comptes
* Système d'inscription et de connexion complet.
* Sécurisation des mots de passe (BCrypt).
* Protection des routes via Spring Security.

## 🛠️ Stack Technique

* **Backend :** Java 17, Spring Boot 3, Spring Security, Spring Data JPA.
* **Frontend :** Thymeleaf (Templating), JavaScript (Fetch API), CSS3 (Animations).
* **Base de Données :** MySQL / Hibernate.
* **Build Tool :** Maven.

## ⚙️ Installation et Démarrage

### Prérequis
* JDK 17 ou supérieur.
* Maven.

### Étapes

1.  **Cloner le projet :**
    ```bash
    git clone [https://github.com/votre-username/pierre-ludmann-chessmate.git](https://github.com/votre-username/pierre-ludmann-chessmate.git)
    cd pierre-ludmann-chessmate
    ```

2. **Lancer l'application :**
    ```bash
    mvn spring-boot:run
    ```

3. **Accès :**
    Ouvrez votre navigateur sur `http://localhost:8080`.

## 📖 Comment Jouer ?

1.  **Choisissez un défi :** Utilisez le menu déroulant à droite pour sélectionner un mode (ex: "8 Dames").
2.  **Sélectionnez une pièce :** Cliquez sur l'icône de la pièce (à gauche) et choisissez sa couleur.
3.  **Placez sur le plateau :** Cliquez sur une case vide.
  * *Si la case est menacée :* Un message d'erreur apparaît.
  * *Si le coup est valide :* La pièce apparaît et le compteur d'objectifs se met à jour.
4.  **Victoire :** Une fois tous les objectifs atteints sans conflit, un message de victoire s'affiche !

## 🏗️ Architecture

Le projet respecte le pattern **MVC (Modèle-Vue-Contrôleur)** :

* **Model :** Entités JPA (`Utilisateur`) et Logique métier (`ChessGame`, `Echiquier`, `Piece`). La logique de validation des attaques (Reines, Cavaliers, etc.) est encapsulée ici.
* **View :** Fichiers HTML Thymeleaf (`show.html`) enrichis par du JavaScript pour la dynamique côté client.
* **Controller :** `ChessController` gère les endpoints REST et les vues, maintient l'état du jeu en session (`@SessionAttributes`).

---
*Développé dans le cadre d'un projet universitaire à l'Université de Lorraine par l'équipe : DI LORETO, DODIN, OUADAH, TULASNE, SIERENS & ZILBERBERG*