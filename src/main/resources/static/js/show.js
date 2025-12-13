// ETAT GLOBAL
let appState = {
    piece: localStorage.getItem('chessPiece') || 'dame',
    isWhite: localStorage.getItem('chessColor') !== 'false',
    mode: localStorage.getItem('chessMode') || 'place',
    isLoading: false
};

document.addEventListener("DOMContentLoaded", () => {
    updateVisuals();
    checkServerMessage(); // Affiche le message s'il y en a un au chargement
});

// --- INTERACTIONS UI ---
function selectPiece(btn) {
    appState.piece = btn.getAttribute('data-piece');
    localStorage.setItem('chessPiece', appState.piece);
    setModePlace();
}
function selectColor(btn) {
    appState.isWhite = btn.getAttribute('data-color') === 'true';
    localStorage.setItem('chessColor', appState.isWhite);
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
        if (btn.getAttribute('data-piece') === appState.piece) btn.classList.add('selected');
        else btn.classList.remove('selected');
    });
    document.querySelectorAll('.color-btn').forEach(btn => {
        if ((btn.getAttribute('data-color') === 'true') === appState.isWhite) btn.classList.add('selected');
        else btn.classList.remove('selected');
    });
    const btnPlace = document.getElementById('modePlace');
    const btnRemove = document.getElementById('modeRemove');
    if (btnPlace && btnRemove) {
        if (appState.mode === 'place') { btnPlace.classList.add('selected'); btnRemove.classList.remove('selected'); }
        else { btnRemove.classList.add('selected'); btnPlace.classList.remove('selected'); }
    }
}

// --- AJAX PRINCIPAL ---

async function clickCase(caseElem) {
    if (appState.isLoading) return;
    const x = caseElem.getAttribute('data-x');
    const y = caseElem.getAttribute('data-y');
    const csrfToken = document.querySelector('input[name="_csrf"]').value;

    const formData = new FormData();
    formData.append('_csrf', csrfToken);
    formData.append('x', x);
    formData.append('y', y);

    let url = (appState.mode === 'place') ? '/place' : '/remove';
    if (appState.mode === 'place') {
        formData.append('pieceType', appState.piece);
        formData.append('estBlanc', appState.isWhite);
    }

    appState.isLoading = true;
    caseElem.style.opacity = '0.5';

    try {
        const response = await fetch(url, { method: 'POST', body: formData });
        if (response.ok) await updatePageContent(response);
    } catch (e) { console.error(e); } finally {
        appState.isLoading = false;
        caseElem.style.opacity = '1';
    }
}

// --- CHANGEMENT DE MODE & CUSTOM ---

async function changeMode(selectElem) {
    const val = selectElem.value;
    if (val === 'custom') {
        document.getElementById('customConfigPanel').style.display = 'block';
        selectElem.disabled = true;
        return;
    }

    const csrfToken = document.querySelector('input[name="_csrf"]').value;
    const formData = new FormData();
    formData.append('_csrf', csrfToken);
    formData.append('modeDeJeu', val);

    document.querySelector('.container').style.opacity = '0.6';
    try {
        const response = await fetch('/changeMode', { method: 'POST', body: formData });
        if (response.ok) await updatePageContent(response);
    } catch (e) { console.error(e); } finally { document.querySelector('.container').style.opacity = '1'; }
}

async function submitCustomConfig() {
    const csrfToken = document.querySelector('input[name="_csrf"]').value;
    const formData = new FormData();
    formData.append('_csrf', csrfToken);

    // IMPORTANT : Les noms 'dame', 'tour' doivent correspondre à ce que le Controller attend
    formData.append('dame', document.getElementById('c_dame').value);
    formData.append('tour', document.getElementById('c_tour').value);
    formData.append('fou', document.getElementById('c_fou').value);
    formData.append('cavalier', document.getElementById('c_cavalier').value);
    formData.append('roi', document.getElementById('c_roi').value);

    try {
        const response = await fetch('/customConfig', { method: 'POST', body: formData });
        if (response.ok) {
            await updatePageContent(response);

            // On vérifie si une erreur est revenue (via le div caché message)
            const errorMsg = document.getElementById('server-data-message');
            // Si le message est de type 'error', on garde le panneau ouvert
            if (errorMsg && errorMsg.dataset.type === 'error') {
                // On ne fait rien, l'utilisateur doit corriger
            } else {
                // Succès : on ferme le panneau
                cancelCustom();
            }
        }
    } catch (e) { console.error(e); }
}

function cancelCustom() {
    document.getElementById('customConfigPanel').style.display = 'none';
    const select = document.getElementById('modeSelect');
    select.disabled = false;
    // Note : Le select restera visuellement sur "Custom" si le serveur a validé le changement
}

// --- MISE A JOUR DU DOM ---

async function updatePageContent(response) {
    const html = await response.text();
    const doc = new DOMParser().parseFromString(html, 'text/html');

    // 1. On remplace le plateau
    document.querySelector('.board-container').innerHTML = doc.querySelector('.board-container').innerHTML;

    // 2. On remplace le panneau d'infos (C'est là que les Objectifs et le Select Mode sont)
    document.querySelector('.info-panel').innerHTML = doc.querySelector('.info-panel').innerHTML;

    // 3. On traite le message s'il y en a un
    const messageData = doc.getElementById('server-data-message');
    if (messageData) {
        showToast(messageData.dataset.msg, messageData.dataset.type);
    }

    // 4. On remet les styles JS (sélection verte) car le HTML a changé
    updateVisuals();
}

function checkServerMessage() {
    const messageData = document.getElementById('server-data-message');
    if (messageData) {
        showToast(messageData.dataset.msg, messageData.dataset.type);
        messageData.remove();
    }
}

// --- TOASTS EN BAS ---
function showToast(message, type) {
    if (!message) return;
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerText = message;
    container.appendChild(toast);
    setTimeout(() => { toast.remove(); }, 3500);
}