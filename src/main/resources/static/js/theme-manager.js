/**
 * Gestion du Thème (Clair / Sombre) pour Chessmate
 */
document.addEventListener('DOMContentLoaded', () => {
    const themeToggle = document.getElementById('theme-toggle');
    const themeIcon = document.getElementById('theme-icon');
    const themeText = document.getElementById('theme-text');
    const body = document.body;

    // 1. Charger le thème sauvegardé ou utiliser la préférence système
    const savedTheme = localStorage.getItem('theme');

    if (savedTheme === 'dark') {
        applyDarkMode();
    } else {
        applyLightMode();
    }

    // 2. Écouteur de clic sur le bouton
    if (themeToggle) {
        themeToggle.addEventListener('click', (e) => {
            e.preventDefault(); // Empêche le saut de page si c'est un lien
            if (body.classList.contains('dark-mode')) {
                applyLightMode();
            } else {
                applyDarkMode();
            }
        });
    }

    // --- Fonctions utilitaires ---

    function applyDarkMode() {
        body.classList.add('dark-mode');
        localStorage.setItem('theme', 'dark');
        if (themeIcon) themeIcon.className = 'fa fa-moon';
        if (themeText) themeText.innerText = 'Mode Sombre';
    }

    function applyLightMode() {
        body.classList.remove('dark-mode');
        localStorage.setItem('theme', 'light');
        if (themeIcon) themeIcon.className = 'fa fa-sun';
        if (themeText) themeText.innerText = 'Mode Clair';
    }
});