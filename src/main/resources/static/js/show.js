/**
 * Script de l'Interface de Jeu (ChessMate) - Version Placement
 */

let appState = {
    piece: localStorage.getItem('chessPiece') || 'Dame',      // "Dame" avec majuscule pour le factory
    isWhite: true,                                           // Le mode placement est souvent en blanc par défaut
    mode: localStorage.getItem('chessMode') || 'place',
    isLoading: false
};

document.addEventListener("DOMContentLoaded", () => {
    updateVisuals();
    checkServerMessage();
});

function selectPiece(btn) {
    // On récupère le type et on s'assure que la première lettre est majuscule pour le backend
    let rawPiece = btn.getAttribute('data-piece');
    appState.piece = rawPiece.charAt(0).toUpperCase() + rawPiece.slice(1);

    localStorage.setItem('chessPiece', appState.piece);
    setModePlace();
}

function selectColor(btn) {
    appState.isWhite = btn.getAttribute('data-color') === 'true';
    updateVisuals();
}

function setModePlace() {
    appState.mode = 'place';
    localStorage.setItem('chessMode', 'place');
    updateVisuals();
}

function setModeRemove() {
    appState.mode = 'remove';
    localStorage.setItem('chessMode', 'remove');
    updateVisuals();
}

function updateVisuals() {
    document.querySelectorAll('.piece-btn').forEach(btn => {
        const btnPiece = btn.getAttribute('data-piece').toLowerCase();
        if (btnPiece === appState.piece.toLowerCase()) btn.classList.add('selected');
        else btn.classList.remove('selected');
    });

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
 * Gère le clic sur une case.
 * Point d'entrée unique : /placement/action
 */
async function clickCase(caseElem) {
    if (appState.isLoading) return;

    const x = caseElem.getAttribute('data-x');
    const y = caseElem.getAttribute('data-y');
    const csrfToken = document.querySelector('input[name="_csrf"]').value;

    const formData = new FormData();
    formData.append('_csrf', csrfToken);
    formData.append('x', x);
    formData.append('y', y);

    // Si on est en mode "place", on envoie le type, sinon on envoie vide (le contrôleur retirera la pièce)
    if (appState.mode === 'place') {
        formData.append('type', appState.piece);
    } else {
        formData.append('type', '');
    }

    appState.isLoading = true;
    caseElem.style.opacity = '0.5';

    try {
        // CORRECTION URL : /placement/action
        const response = await fetch('/placement/action', { method: 'POST', body: formData });
        if (response.ok) await updatePageContent(response);
    } catch (e) {
        console.error('Erreur:', e);
    } finally {
        appState.isLoading = false;
        caseElem.style.opacity = '1';
    }
}

/**
 * Changement de mode de jeu (8 Reines, etc.)
 */
async function changeMode(selectElem) {
    const val = selectElem.value;

    if (val === 'custom') {
        document.getElementById('customConfigPanel').style.display = 'block';
        return;
    }

    const csrfToken = document.querySelector('input[name="_csrf"]').value;
    const formData = new FormData();
    formData.append('_csrf', csrfToken);
    formData.append('modeDeJeu', val);

    try {
        // CORRECTION URL : /placement/changeMode
        const response = await fetch('/placement/changeMode', { method: 'POST', body: formData });
        if (response.ok) await updatePageContent(response);
    } catch (e) {
        console.error('Erreur changement de mode:', e);
    }
}

/**
 * Soumission de la config personnalisée
 */
async function submitCustomConfig() {
    const csrfToken = document.querySelector('input[name="_csrf"]').value;
    const formData = new FormData();
    formData.append('_csrf', csrfToken);

    // CORRECTION : Les clés doivent correspondre aux "c_dame" attendus par ton parseConfigParam
    formData.append('c_dame', document.getElementById('c_dame').value);
    formData.append('c_tour', document.getElementById('c_tour').value);
    formData.append('c_fou', document.getElementById('c_fou').value);
    formData.append('c_cavalier', document.getElementById('c_cavalier').value);
    formData.append('c_roi', document.getElementById('c_roi').value);

    try {
        // CORRECTION URL : /placement/customConfig
        const response = await fetch('/placement/customConfig', { method: 'POST', body: formData });
        if (response.ok) {
            await updatePageContent(response);
            // Ferme le panneau si pas d'erreur affichée
            const errorMsg = document.getElementById('server-data-message');
            if (!(errorMsg && errorMsg.dataset.type === 'error')) {
                cancelCustom();
            }
        }
    } catch (e) {
        console.error('Erreur config custom:', e);
    }
}

function cancelCustom() {
    document.getElementById('customConfigPanel').style.display = 'none';
    document.getElementById('modeSelect').disabled = false;
}

/**
 * Mise à jour partielle du DOM
 */
async function updatePageContent(response) {
    const html = await response.text();
    const doc = new DOMParser().parseFromString(html, 'text/html');

    // Mise à jour des zones dynamiques
    const board = doc.querySelector('.board-container');
    const info = doc.querySelector('.info-panel');
    const message = doc.getElementById('server-data-message');

    if (board) document.querySelector('.board-container').innerHTML = board.innerHTML;
    if (info) document.querySelector('.info-panel').innerHTML = info.innerHTML;

    if (message) {
        showToast(message.dataset.msg, message.dataset.type);
    }

    updateVisuals();
}

function checkServerMessage() {
    const messageData = document.getElementById('server-data-message');
    if (messageData) {
        showToast(messageData.dataset.msg, messageData.dataset.type);
        // On ne le supprime pas forcément ici car updatePageContent s'en charge au prochain clic
    }
}

function showToast(message, type) {
    if (!message) return;
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerText = message;
    container.appendChild(toast);
    setTimeout(() => { toast.style.opacity = '0'; setTimeout(() => toast.remove(), 500); }, 3000);
}