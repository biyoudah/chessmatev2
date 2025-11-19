// 1. On force la pièce à 'dame' (Reine)
// On ne met plus 'null' pour éviter les erreurs.
let selectedPiece = 'dame';

let selectedColor = true; // true = blanc, par défaut
let mode = 'place';       // 'place' par défaut

// Initialisation au chargement
document.addEventListener('DOMContentLoaded', () => {
    // Initialise le visuel du bouton Mode Placer (Vert)
    const btnPlace = document.getElementById('modePlace');
    if (btnPlace) btnPlace.style.background = '#4CAF50';

    // Force la valeur de la couleur dans le formulaire caché
    const inputBlanc = document.getElementById('estBlanc');
    if (inputBlanc) inputBlanc.value = 'true';
});

// --- Gestion de la Couleur (Reste inchangé pour le contrôleur) ---
document.querySelectorAll('.color-btn').forEach(btn => {
    btn.addEventListener('click', function() {
        document.querySelectorAll('.color-btn').forEach(b => b.classList.remove('selected'));
        this.classList.add('selected');
        selectedColor = this.getAttribute('data-color') === 'true';
        document.getElementById('estBlanc').value = selectedColor;
    });
});

// --- Gestion des Modes (Placer / Retirer) ---
document.getElementById('modePlace').addEventListener('click', function() {
    mode = 'place';
    this.style.background = '#4CAF50';
    document.getElementById('modeRemove').style.background = '#ccc';
});

document.getElementById('modeRemove').addEventListener('click', function() {
    mode = 'remove';
    this.style.background = '#f44336';
    document.getElementById('modePlace').style.background = '#ccc';
});

// --- Clic sur l'échiquier (Logique critique pour le contrôleur) ---
function clickCase(element) {
    const x = element.getAttribute('data-x');
    const y = element.getAttribute('data-y');

    if (mode === 'place') {
        // Remplissage du formulaire caché
        document.getElementById('placeX').value = x;
        document.getElementById('placeY').value = y;

        // ICI : On envoie toujours "dame" au contrôleur
        document.getElementById('pieceType').value = selectedPiece;

        // La couleur est gérée par le bouton couleur
        document.getElementById('placeForm').submit();
    } else {
        // Mode Retirer
        document.getElementById('removeX').value = x;
        document.getElementById('removeY').value = y;
        document.getElementById('removeForm').submit();
    }
}