let selectedPiece = null;
let selectedColor = true;
let mode = 'place';

function clickCase(caseElement) {
    const x = caseElement.getAttribute('data-x');
    const y = caseElement.getAttribute('data-y');

    if (mode === 'place') {
        if (!selectedPiece) {
            alert('Veuillez d\'abord sélectionner une pièce !');
            return;
        }

        document.getElementById('placeX').value = x;
        document.getElementById('placeY').value = y;
        document.getElementById('pieceType').value = selectedPiece;
        document.getElementById('estBlanc').value = selectedColor;
        document.getElementById('placeForm').submit();

    } else if (mode === 'remove') {
        document.getElementById('removeX').value = x;
        document.getElementById('removeY').value = y;
        document.getElementById('removeForm').submit();
    }
}

function selectPiece(button) {
    document.querySelectorAll('.piece-btn').forEach(b => b.classList.remove('selected'));
    button.classList.add('selected');
    selectedPiece = button.getAttribute('data-piece');
}

function selectColor(button) {
    document.querySelectorAll('.color-btn').forEach(b => b.classList.remove('selected'));
    button.classList.add('selected');
    selectedColor = button.getAttribute('data-color') === 'true';
    document.getElementById('estBlanc').value = selectedColor;
}

function setModePlace() {
    mode = 'place';
    document.getElementById('modePlace').classList.add('selected');
    document.getElementById('modeRemove').classList.remove('selected');
}

function setModeRemove() {
    mode = 'remove';
    document.getElementById('modeRemove').classList.add('selected');
    document.getElementById('modePlace').classList.remove('selected');}