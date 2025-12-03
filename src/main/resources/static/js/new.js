function selectMode(mode, buttonId) {
    // met à jour la valeur envoyée au contrôleur
    document.getElementById('modeDeJeu').value = mode;

    // retire la classe active des deux boutons
    document.querySelectorAll('.game-btn').forEach(b => b.classList.remove('active-mode'));

    // ajoute la classe active sur le bouton cliqué
    document.getElementById(buttonId).classList.add('active-mode');

    // met à jour le texte d’info
    updateModeInfo(mode);
}

function showModeHover(label) {
    // survol : on affiche juste le texte lisible
}

function updateModeInfo(mode) {
    let label = mode === '8-queens' ? '8 Reines' : 'Custom';
}
