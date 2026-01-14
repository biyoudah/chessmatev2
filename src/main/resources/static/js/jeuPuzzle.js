// URLs fixes (statiques)
const moveUrl = '/puzzle/move';
const compMoveUrl = '/puzzle/computer-move';
const resetUrl = '/puzzle/reset';
const changeModeUrl = '/puzzle/changeMode';
const clearUrl = '/puzzle/clear';
const hintUrl = '/puzzle/hint';

let startCase = null;

// Fonction utilitaire pour les requêtes POST
async function sendRequest(url, fd = new FormData()) {
    const token = document.querySelector('input[name="_csrf"]')?.value;
    const loader = document.getElementById('board-loader');
    if (url.includes('reset') || url.includes('changeMode')) {
        if (loader) loader.style.display = 'flex';
    }
    try {
        const response = await fetch(url, {
            method: 'POST',
            body: fd,
            headers: { 'X-CSRF-TOKEN': token }
        });
        if (response.redirected) {
            window.location.href = response.url;
        } else {
            window.location.reload();
        }
    } catch(e) {
        console.error(e);
        if (loader) loader.style.display = 'none';
    }
}

// Changement de difficulté
function triggerChangeMode(v) {
    const loader = document.getElementById('board-loader');
    if (loader) loader.style.display = 'flex';
    const fd = new FormData();
    fd.append('difficulte', v);
    sendRequest(changeModeUrl, fd);
}

// Reset puzzle
function triggerReset() {
    const loader = document.getElementById('board-loader');
    if (loader) loader.style.display = 'flex';
    sendRequest(resetUrl);
}

// Clic sur case (sélection/déplacement)
function clickPuzzleCase(el) {
    document.querySelectorAll('.hint-anim').forEach(e => e.classList.remove('hint-anim'));
    const x = el.getAttribute('data-x');
    const y = el.getAttribute('data-y');
    const hasPiece = el.querySelector('.piece') !== null;

    if (!startCase) {
        if (hasPiece) {
            startCase = el;
            el.classList.add('selected');
            el.style.backgroundColor = "rgba(255, 215, 0, 0.4)";
        }
    } else {
        const dx = startCase.getAttribute('data-x');
        const dy = startCase.getAttribute('data-y');
        startCase.style.backgroundColor = "";
        startCase.classList.remove('selected');
        if (dx === x && dy === y) {
            startCase = null;
            return;
        }
        const fd = new FormData();
        fd.append('departX', dx);
        fd.append('departY', dy);
        fd.append('arriveeX', x);
        fd.append('arriveeY', y);
        sendRequest(moveUrl, fd);
    }
}

// Indice et abandon
function triggerHint() {
    sendRequest(hintUrl);
}
function triggerClear() {
    sendRequest(clearUrl);
}

// Auto-play ordi si nécessaire (au chargement)
document.addEventListener('DOMContentLoaded', function() {
    if (typeof window.ordiJoue !== 'undefined' && window.ordiJoue) {
        const thinkingLoader = document.getElementById('thinking-loader');
        if (thinkingLoader) thinkingLoader.style.display = 'block';
        setTimeout(() => sendRequest(compMoveUrl), 500);
    }
    const diffSelect = document.getElementById('diffSelect');
    if (diffSelect) {
        diffSelect.addEventListener('change', function() {
            triggerChangeMode(this.value);
        });
    }
    const resetBtn = document.getElementById('resetBtn');
    if (resetBtn) resetBtn.addEventListener('click', triggerReset);

    const hintBtn = document.getElementById('hintBtn');
    if (hintBtn) hintBtn.addEventListener('click', triggerHint);

    const clearBtn = document.getElementById('clearBtn');
    if (clearBtn) clearBtn.addEventListener('click', triggerClear);
});