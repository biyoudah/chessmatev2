// 1. On récupère les valeurs sauvegardées (ou on met des valeurs par défaut)
let selectedPiece = localStorage.getItem('chessPiece') || 'dame';
let selectedColor = localStorage.getItem('chessColor') !== 'false'; // Par défaut true (Blanc)
let mode = localStorage.getItem('chessMode') || 'place';

// 2. Au chargement de la page, on met à jour l'interface visuelle
document.addEventListener("DOMContentLoaded", function() {
    // Restaurer la sélection visuelle de la pièce
    const pieceBtn = document.querySelector(`.piece-btn[data-piece="${selectedPiece}"]`);
    if (pieceBtn) pieceBtn.classList.add('selected');

    // Restaurer la sélection visuelle de la couleur
    const colorString = selectedColor ? 'true' : 'false';
    const colorBtn = document.querySelector(`.color-btn[data-color="${colorString}"]`);
    document.querySelectorAll('.color-btn').forEach(b => b.classList.remove('selected'));
    if (colorBtn) colorBtn.classList.add('selected');

    // Restaurer la sélection visuelle du mode
    if (mode === 'place') {
        document.getElementById('modePlace').classList.add('selected');
        document.getElementById('modeRemove').classList.remove('selected');
    } else {
        document.getElementById('modeRemove').classList.add('selected');
        document.getElementById('modePlace').classList.remove('selected');
    }

    // Mettre à jour l'input caché pour la couleur dès le début
    const estBlancInput = document.getElementById('estBlanc');
    if(estBlancInput) estBlancInput.value = selectedColor;
});

// --- Fonctions d'action ---

function clickCase(caseElement) {
    const x = caseElement.getAttribute('data-x');
    const y = caseElement.getAttribute('data-y');

    if (mode === 'place') {
        if (!selectedPiece) {
            alert('Erreur: Aucune pièce sélectionnée.');
            return;
        }

        // Remplissage du formulaire "Placer"
        document.getElementById('placeX').value = x;
        document.getElementById('placeY').value = y;
        document.getElementById('pieceType').value = selectedPiece;
        document.getElementById('estBlanc').value = selectedColor;

        document.getElementById('placeForm').submit();

    } else if (mode === 'remove') {
        // Remplissage du formulaire "Retirer"
        document.getElementById('removeX').value = x;
        document.getElementById('removeY').value = y;

        document.getElementById('removeForm').submit();
    }
}

function selectPiece(button) {
    // Gestion visuelle
    document.querySelectorAll('.piece-btn').forEach(b => b.classList.remove('selected'));
    button.classList.add('selected');

    // Mise à jour de la variable et du stockage
    selectedPiece = button.getAttribute('data-piece');
    localStorage.setItem('chessPiece', selectedPiece);

    // Si on change de pièce, on repasse logiquement en mode "Placer"
    setModePlace();
}

function selectColor(button) {
    // Gestion visuelle
    document.querySelectorAll('.color-btn').forEach(b => b.classList.remove('selected'));
    button.classList.add('selected');

    // Mise à jour variable et stockage
    selectedColor = button.getAttribute('data-color') === 'true';
    document.getElementById('estBlanc').value = selectedColor;
    localStorage.setItem('chessColor', selectedColor); // Stocke "true" ou "false"

    // Mise à jour de l'input caché
    const estBlancInput = document.getElementById('estBlanc');
    if(estBlancInput) estBlancInput.value = selectedColor;
}

function setModePlace() {
    mode = 'place';
    localStorage.setItem('chessMode', 'place');

    document.getElementById('modePlace').classList.add('selected');
    document.getElementById('modeRemove').classList.remove('selected');
}

function setModeRemove() {
    mode = 'remove';
    localStorage.setItem('chessMode', 'remove');

    document.getElementById('modeRemove').classList.add('selected');
    document.getElementById('modePlace').classList.remove('selected');
}