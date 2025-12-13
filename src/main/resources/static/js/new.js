/**
 * Script de gestion de la sélection du mode de jeu (ChessMate).
 *
 * Ce fichier gère l'interactivité de la page "Nouvelle Partie". Il permet à l'utilisateur
 * de basculer entre les différents modes (ex: 8 Reines, Custom), met à jour les champs
 * de formulaire cachés et gère le retour visuel (classes CSS actives).
 */

/**
 * Sélectionne un mode de jeu et met à jour l'interface utilisateur.
 *
 * Cette fonction est déclenchée lorsqu'un utilisateur clique sur un bouton de mode.
 * Elle assure la synchronisation entre le choix visuel et la valeur qui sera envoyée au serveur.
 *
 * @param {string} mode - L'identifiant technique du mode (ex: '8-queens' ou 'custom').
 * @param {string} buttonId - L'ID HTML du bouton cliqué, utilisé pour cibler l'élément DOM.
 */
function selectMode(mode, buttonId) {
    // Mise à jour du champ caché (input hidden) qui sera soumis avec le formulaire
    document.getElementById('modeDeJeu').value = mode;

    // Réinitialisation : On retire la classe 'active-mode' de tous les boutons de jeu
    document.querySelectorAll('.game-btn').forEach(b => b.classList.remove('active-mode'));

    // Activation : On ajoute la classe 'active-mode' uniquement au bouton cliqué (feedback visuel)
    document.getElementById(buttonId).classList.add('active-mode');

    // Mise à jour des informations textuelles associées au mode choisi
    updateModeInfo(mode);
}

/**
 * Gère l'interaction de survol (hover) sur les boutons de mode.
 *
 * Cette fonction est destinée à fournir un aperçu ou une aide contextuelle
 * lorsque l'utilisateur hésite sur un choix.
 *
 * @param {string} label - Le libellé du mode actuellement survolé.
 */
function showModeHover(label) {
    // Cette fonction est prévue pour afficher une info-bulle ou une description dynamique.
    // Pour l'instant, elle sert principalement au débogage via la console.
    // TODO: Implémenter l'affichage d'une tooltip dans l'UI.
    console.log('Hover on: ' + label);
}

/**
 * Met à jour les informations affichées à l'écran en fonction du mode sélectionné.
 *
 * Convertit l'identifiant technique du mode en un libellé lisible pour l'utilisateur
 * et prépare l'affichage de la description correspondante.
 *
 * @param {string} mode - Le mode de jeu sélectionné.
 */
function updateModeInfo(mode) {
    // Création d'un libellé convivial ("User-friendly") basé sur l'ID du mode
    let label = mode === '8-queens' ? '8 Reines' : 'Custom';

    // Logique d'affichage (Actuellement en attente d'implémentation DOM complète).
    // Cette section devrait cibler un élément HTML (ex: #mode-info-text) pour y injecter le libellé.
    // Exemple : document.getElementById('mode-info-text').textContent = 'Mode : ' + label;
}