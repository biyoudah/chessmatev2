/**
 * Script de l'Interface de Jeu (ChessMate)
 *
 * Ce script pilote toute la logique côté client de la page de jeu.
 * Il gère les interactions avec le plateau, la sélection des pièces,
 * la communication asynchrone (AJAX/Fetch) avec le serveur Spring Boot,
 * et la persistance des préférences utilisateur.
 */

/**
 * État global de l'application.
 *
 * Stocke les préférences de l'utilisateur (pièce sélectionnée, couleur, mode)
 * et utilise le `localStorage` pour les conserver même après un rafraîchissement de page.
 */
let appState = {
    piece: localStorage.getItem('chessPiece') || 'dame',      // Type de pièce actif (ex: 'dame')
    isWhite: localStorage.getItem('chessColor') !== 'false',  // Couleur active (true = blanc)
    mode: localStorage.getItem('chessMode') || 'place',       // Mode d'interaction ('place' ou 'remove')
    isLoading: false  // Verrou pour empêcher les requêtes simultanées (anti-spam clic)
};

/**
 * Point d'entrée : Initialisation au chargement de la page.
 */
document.addEventListener("DOMContentLoaded", () => {
    // Applique les états visuels (boutons sélectionnés) selon les préférences stockées
    updateVisuals();
    // Vérifie s'il y a des messages du serveur (succès/erreur) à afficher immédiatement
    checkServerMessage();
});

/**
 * Gère le clic sur un bouton de sélection de pièce.
 *
 * @param {HTMLElement} btn - L'élément bouton cliqué.
 */
function selectPiece(btn) {
    // Met à jour l'état interne
    appState.piece = btn.getAttribute('data-piece');
    // Persistance
    localStorage.setItem('chessPiece', appState.piece);
    // Bascule automatiquement en mode "Placer" pour une meilleure UX
    setModePlace();
}

/**
 * Gère le clic sur un bouton de sélection de couleur.
 *
 * @param {HTMLElement} btn - L'élément bouton cliqué.
 */
function selectColor(btn) {
    appState.isWhite = btn.getAttribute('data-color') === 'true';
    localStorage.setItem('chessColor', appState.isWhite);
    updateVisuals();
}

/**
 * Active le mode "Placer une pièce".
 */
function setModePlace() {
    appState.mode = 'place';
    localStorage.setItem('chessMode', 'place');
    updateVisuals();
}

/**
 * Active le mode "Retirer une pièce".
 */
function setModeRemove() {
    appState.mode = 'remove';
    localStorage.setItem('chessMode', 'remove');
    updateVisuals();
}

/**
 * Met à jour l'apparence des boutons de contrôle.
 *
 * Parcourt tous les boutons (pièces, couleurs, modes) et ajoute/retire
 * la classe CSS 'selected' en fonction de l'état actuel `appState`.
 */
function updateVisuals() {
    // Gestion des boutons de pièces
    document.querySelectorAll('.piece-btn').forEach(btn => {
        if (btn.getAttribute('data-piece') === appState.piece) btn.classList.add('selected');
        else btn.classList.remove('selected');
    });

    // Gestion des boutons de couleur
    document.querySelectorAll('.color-btn').forEach(btn => {
        if ((btn.getAttribute('data-color') === 'true') === appState.isWhite) btn.classList.add('selected');
        else btn.classList.remove('selected');
    });

    // Gestion des boutons de mode (Placer vs Retirer)
    const btnPlace = document.getElementById('modePlace');
    const btnRemove = document.getElementById('modeRemove');
    if (btnPlace && btnRemove) {
        if (appState.mode === 'place') {
            btnPlace.classList.add('selected');
            btnRemove.classList.remove('selected');
        } else {
            btnRemove.classList.add('selected');
            btnPlace.classList.remove('selected');
        }
    }
}

/**
 * Gère le clic sur une case du plateau d'échecs.
 *
 * Cette fonction construit une requête POST vers le serveur pour appliquer
 * l'action (placer ou retirer) sans recharger la page.
 *
 * @param {HTMLElement} caseElem - L'élément DOM de la case cliquée.
 */
async function clickCase(caseElem) {
    // Protection contre les doubles clics rapides
    if (appState.isLoading) return;

    // Récupération des données contextuelles
    const x = caseElem.getAttribute('data-x');
    const y = caseElem.getAttribute('data-y');
    // Récupération du token CSRF (sécurité Spring Security obligatoire pour les POST)
    const csrfToken = document.querySelector('input[name="_csrf"]').value;

    // Préparation des données du formulaire
    const formData = new FormData();
    formData.append('_csrf', csrfToken);
    formData.append('x', x);
    formData.append('y', y);

    // Détermination de l'endpoint API selon le mode
    let url = (appState.mode === 'place') ? '/place' : '/remove';

    // Ajout des infos spécifiques si on place une pièce
    if (appState.mode === 'place') {
        formData.append('pieceType', appState.piece);
        formData.append('estBlanc', appState.isWhite);
    }

    // Feedback visuel immédiat (la case s'assombrit)
    appState.isLoading = true;
    caseElem.style.opacity = '0.5';

    try {
        // Envoi asynchrone
        const response = await fetch(url, { method: 'POST', body: formData });
        // Si succès, on met à jour le HTML de la page
        if (response.ok) await updatePageContent(response);
    } catch (e) {
        console.error('Erreur lors de l\'interaction avec le plateau:', e);
    } finally {
        // Nettoyage de l'état
        appState.isLoading = false;
        caseElem.style.opacity = '1';
    }
}

/**
 * Gère le changement de mode via le menu déroulant (Select).
 *
 * @param {HTMLSelectElement} selectElem - L'élément <select> modifié.
 */
async function changeMode(selectElem) {
    const val = selectElem.value;

    // Cas spécial : Mode personnalisé (ouvre une modale/panneau)
    if (val === 'custom') {
        document.getElementById('customConfigPanel').style.display = 'block';
        selectElem.disabled = true; // Empêche de changer pendant la config
        return;
    }

    // Cas standard : Changement de défi (ex: 8 Reines, Cavaliers...)
    const csrfToken = document.querySelector('input[name="_csrf"]').value;
    const formData = new FormData();
    formData.append('_csrf', csrfToken);
    formData.append('modeDeJeu', val);

    // Feedback visuel global
    document.querySelector('.container').style.opacity = '0.6';

    try {
        const response = await fetch('/changeMode', { method: 'POST', body: formData });
        if (response.ok) await updatePageContent(response);
    } catch (e) {
        console.error('Erreur changement de mode:', e);
    } finally {
        document.querySelector('.container').style.opacity = '1';
    }
}

/**
 * Soumet la configuration personnalisée (nombre de pièces max).
 */
async function submitCustomConfig() {
    const csrfToken = document.querySelector('input[name="_csrf"]').value;
    const formData = new FormData();
    formData.append('_csrf', csrfToken);

    // Récupération des valeurs des inputs (IMPORTANT : doit correspondre au Controller Java)
    formData.append('dame', document.getElementById('c_dame').value);
    formData.append('tour', document.getElementById('c_tour').value);
    formData.append('fou', document.getElementById('c_fou').value);
    formData.append('cavalier', document.getElementById('c_cavalier').value);
    formData.append('roi', document.getElementById('c_roi').value);

    try {
        const response = await fetch('/customConfig', { method: 'POST', body: formData });
        if (response.ok) {
            await updatePageContent(response);

            // Vérification s'il y a une erreur renvoyée par le serveur
            const errorMsg = document.getElementById('server-data-message');

            // Si erreur, on laisse le panneau ouvert pour correction
            if (errorMsg && errorMsg.dataset.type === 'error') {
                // Ne rien faire, l'utilisateur doit corriger
            } else {
                // Succès : on ferme le panneau
                cancelCustom();
            }
        }
    } catch (e) {
        console.error('Erreur config custom:', e);
    }
}

/**
 * Annule la configuration personnalisée et ferme le panneau.
 */
function cancelCustom() {
    document.getElementById('customConfigPanel').style.display = 'none';
    const select = document.getElementById('modeSelect');
    select.disabled = false;
    // Note : Le select reste visuellement sur "Custom" si le serveur a validé le changement
}

/**
 * Met à jour le contenu de la page dynamiquement.
 *
 * Cette fonction simule une SPA (Single Page Application) en remplaçant
 * uniquement les parties modifiées du HTML (le plateau et le panneau d'infos)
 * à partir de la réponse complète renvoyée par le serveur.
 *
 * @param {Response} response - La réponse fetch brute du serveur.
 */
async function updatePageContent(response) {
    // Conversion de la réponse texte en document HTML virtuel
    const html = await response.text();
    const doc = new DOMParser().parseFromString(html, 'text/html');

    // 1. Remplacement à chaud du plateau de jeu
    document.querySelector('.board-container').innerHTML = doc.querySelector('.board-container').innerHTML;

    // 2. Remplacement du panneau d'informations (Objectifs, Compteurs)
    document.querySelector('.info-panel').innerHTML = doc.querySelector('.info-panel').innerHTML;

    // 3. Traitement des messages flash (Toast)
    const messageData = doc.getElementById('server-data-message');
    if (messageData) {
        showToast(messageData.dataset.msg, messageData.dataset.type);
    }

    // 4. Réapplication des états visuels (car le HTML a été écrasé)
    updateVisuals();
}

/**
 * Vérifie la présence de messages serveur au chargement initial.
 * (Utilisé pour les redirections ou chargements de page classiques).
 */
function checkServerMessage() {
    const messageData = document.getElementById('server-data-message');
    if (messageData) {
        showToast(messageData.dataset.msg, messageData.dataset.type);
        messageData.remove(); // Nettoyage pour ne pas réafficher
    }
}

/**
 * Affiche une notification flottante (Toast).
 *
 * @param {string} message - Le texte à afficher.
 * @param {string} type - Le type de message ('success', 'error', 'warning').
 */
function showToast(message, type) {
    if (!message) return;

    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`; // ex: class="toast success"
    toast.innerText = message;
    container.appendChild(toast);

    // Suppression automatique après 3.5 secondes
    setTimeout(() => { toast.remove(); }, 3500);
}