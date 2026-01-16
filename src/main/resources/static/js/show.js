/**
 * Logique Chessmate - Placement Corrigée
 */

let appState = {
    // On s'assure d'avoir une valeur par défaut cohérente avec le backend
    piece: localStorage.getItem('chessPiece') || 'Dame',
    mode: localStorage.getItem('chessMode') || 'place',
    color: localStorage.getItem('chessColor') || 'true',
    isLoading: false
};

document.addEventListener("DOMContentLoaded", () => {
    updateVisuals();
    checkServerMessage();
});

function playSFX(type) {
    if (!type) return;
    const audio = document.getElementById(`sfx-${type}`);
    if (audio) {
        audio.currentTime = 0;
        audio.play().catch(e => {});
    }
}

async function selectPiece(btn) {
    if (appState.isLoading) return;

    let raw = btn.getAttribute('data-piece');
    // Formatage strict pour le backend (Majuscule initiale)
    appState.piece = raw.charAt(0).toUpperCase() + raw.slice(1);
    localStorage.setItem('chessPiece', appState.piece);

    // Mise à jour visuelle immédiate
    updateVisuals();

    const csrf = document.querySelector('input[name="_csrf"]').value;
    const formData = new FormData();
    formData.append('_csrf', csrf);
    formData.append('type', appState.piece);

    appState.isLoading = true;
    try {
        const res = await fetch('/placement/selectPiece', { method: 'POST', body: formData });
        if (res.ok) await updatePageContent(res);
    } catch (e) {
        console.error("Erreur sélection", e);
    } finally {
        appState.isLoading = false;
        setModePlace(); // Force le passage en mode placement
    }
}

async function selectColor(btn) {
    if (appState.isLoading) return;

    // 1. Récupération de la couleur (data-color="true" ou "false")
    const colorValue = btn.getAttribute('data-color');

    // 2. Mise à jour de l'état local et du stockage
    appState.color = colorValue;
    localStorage.setItem('chessColor', appState.color);

    // 3. Mise à jour visuelle (classe selected)
    updateVisuals();

    // 4. Envoi au serveur (si ton backend en a besoin, ex: '/placement/setColor')
    // Si ton backend n'a pas de endpoint pour juste changer la couleur, tu peux ignorer cette partie fetch.
    const csrf = document.querySelector('input[name="_csrf"]').value;
    const formData = new FormData();
    formData.append('_csrf', csrf);
    formData.append('isWhite', appState.color); // Adapte le nom du paramètre selon ton Controller

    appState.isLoading = true;
    try {
        // Remplace '/placement/setColor' par la bonne URL si elle existe
        const res = await fetch('/placement/setColor', { method: 'POST', body: formData });
        if (res.ok) await updatePageContent(res);
    } catch (e) {
        console.error("Erreur changement couleur", e);
    } finally {
        appState.isLoading = false;
    }
}
async function clickCase(caseElem) {
    if (appState.isLoading) return;

    const csrf = document.querySelector('input[name="_csrf"]').value;
    const formData = new FormData();
    formData.append('_csrf', csrf);
    formData.append('x', caseElem.dataset.x);
    formData.append('y', caseElem.dataset.y);

    // On utilise la pièce stockée dans l'état global
    formData.append('type', appState.mode === 'place' ? appState.piece : '');

    // Si on est en mode "placer", on envoie aussi la couleur
    if (appState.mode === 'place') {
        // Envoie 'true' (string) ou 'false' (string) selon l'état
        formData.append('isWhite', appState.color);
    }

    appState.isLoading = true;
    try {
        const res = await fetch('/placement/action', { method: 'POST', body: formData });
        if (res.ok) await updatePageContent(res);
    } catch (e) {
        playSFX('error');
    } finally {
        appState.isLoading = false;
    }
}

async function updatePageContent(response) {
    const html = await response.text();
    const doc = new DOMParser().parseFromString(html, 'text/html');

    const newContent = doc.querySelector('.main-container');
    if (newContent) {
        document.querySelector('.main-container').innerHTML = newContent.innerHTML;
    }

    // --- NOUVELLE LOGIQUE POUR L'AUTOHIDE ---
    const alert = document.querySelector('.messages-wrapper .alert');
    if (alert) {
        // On attend 5 secondes (5000ms)
        setTimeout(() => {
            // On retire l'animation d'entrée et on met celle de sortie
            alert.classList.remove('animate__fadeInDown');
            alert.classList.add('animate__fadeOutUp');

            // On attend la fin de l'animation de sortie (800ms) pour supprimer l'élément
            setTimeout(() => {
                alert.remove();
            }, 800);
        }, 5000);
    }
    // ----------------------------------------

    // Gestion des sons et effets (garder le reste de ta fonction)
    const data = document.getElementById('server-data-message');
    if (data && data.dataset.msg) {
        playSFX(data.dataset.sound);
        if (data.dataset.type === 'error') {
            const board = document.getElementById('board');
            if (board) {
                board.classList.add('shake');
                setTimeout(() => board.classList.remove('shake'), 500);
            }
        }
        if (data.dataset.type === 'victory' && typeof lancerConfettis === "function") {
            lancerConfettis();
        }
    }

    updateVisuals();
}

function playSFX(type) {
    const audio = document.getElementById(`sfx-${type}`);
    if (audio) {
        audio.currentTime = 0;
        audio.play().catch(e => console.log("Audio blocké par le navigateur"));
    }
}

function changeMode(select) {
    const mode = select.value;
    const customPanel = document.getElementById('customConfigPanel');

    if (mode === 'custom') {
        customPanel.style.display = 'block';
    } else {
        customPanel.style.display = 'none';

        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '/placement/changeMode';

        const inputMode = document.createElement('input');
        inputMode.type = 'hidden';
        inputMode.name = 'modeDeJeu';
        inputMode.value = mode;
        form.appendChild(inputMode);

        const csrfToken = document.querySelector('input[name="_csrf"]').value;
        const inputCsrf = document.createElement('input');
        inputCsrf.type = 'hidden';
        inputCsrf.name = '_csrf';
        inputCsrf.value = csrfToken;
        form.appendChild(inputCsrf);

        document.body.appendChild(form);
        form.submit();
    }
}

function setModePlace() { appState.mode = 'place'; localStorage.setItem('chessMode', 'place'); updateVisuals(); }
function setModeRemove() { appState.mode = 'remove'; localStorage.setItem('chessMode', 'remove'); updateVisuals(); }

function updateVisuals() {
    document.querySelectorAll('.piece-btn').forEach(b => {
        if(b.getAttribute('data-piece').toLowerCase() === appState.piece.toLowerCase()) b.classList.add('selected');
        else b.classList.remove('selected');
    });
    const p = document.getElementById('modePlace'), r = document.getElementById('modeRemove');
    if(p && r) {
        if(appState.mode === 'place') { p.classList.add('selected'); r.classList.remove('selected'); }
        else { r.classList.add('selected'); p.classList.remove('selected'); }
    }

    document.querySelectorAll('.color-btn').forEach(b => {
        // On compare data-color (string) avec appState.color (string)
        if (b.getAttribute('data-color') === String(appState.color)) {
            b.classList.add('selected');
        } else {
            b.classList.remove('selected');
        }
    });
}

function checkServerMessage() {
    const data = document.getElementById('server-data-message');
    if (data && data.dataset.msg && data.dataset.msg.trim() !== "") {
        showToast(data.dataset.msg, data.dataset.type);
        playSFX(data.dataset.sound);
        data.dataset.msg = "";
    }
}

function showToast(m, t) {
    const c = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${t}`;
    toast.innerText = m;
    c.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = "0";
        toast.style.transform = "translateX(100%)";
        setTimeout(() => toast.remove(), 500);
    }, 4000);
}

async function resetBoard() {
    if (appState.isLoading) return;

    const csrf = document.querySelector('input[name="_csrf"]').value;
    const formData = new FormData();
    formData.append('_csrf', csrf);

    appState.isLoading = true;
    try {
        // On appelle l'URL qui correspond à ta méthode reset dans le contrôleur
        const res = await fetch('/placement/reset', { method: 'POST', body: formData });

        // Si ça marche, on met à jour SEULEMENT le contenu de la page
        if (res.ok) {
            await updatePageContent(res);
            playSFX('remove'); // Petit son sympa (optionnel)
        }
    } catch (e) {
        console.error("Erreur reset", e);
    } finally {
        appState.isLoading = false;
    }
}

document.addEventListener('DOMContentLoaded', function() {
    const flashType = "[[${session.flashType}]]";

    if (flashType === 'victory') {
        lancerConfettis(); // Si vous utilisez une lib comme canvas-confetti
    } else if (flashType === 'error') {
        vibrerEcran(); // Petit effet CSS de secousse (shake) sur l'échiquier
    }
});
