/**
 * ChessMate Game Mode Selection Script
 * This script handles the game mode selection functionality on the new game page.
 */

/**
 * Selects a game mode and updates the UI accordingly
 * 
 * @param {string} mode - The game mode identifier ('8-queens' or 'custom')
 * @param {string} buttonId - The HTML id of the button that was clicked
 */
function selectMode(mode, buttonId) {
    // Update the hidden input value that will be sent to the controller
    document.getElementById('modeDeJeu').value = mode;

    // Remove the active class from all game mode buttons
    document.querySelectorAll('.game-btn').forEach(b => b.classList.remove('active-mode'));

    // Add the active class to the clicked button
    document.getElementById(buttonId).classList.add('active-mode');

    // Update the information text about the selected mode
    updateModeInfo(mode);
}

/**
 * Displays information when hovering over a game mode button
 * 
 * @param {string} label - The label of the mode to display on hover
 */
function showModeHover(label) {
    // This function is intended to show a tooltip or information
    // when hovering over a game mode button
    // Currently not implemented - could display information in a tooltip or info box
    console.log('Hover on: ' + label);
}

/**
 * Updates the information display based on the selected game mode
 * 
 * @param {string} mode - The selected game mode
 */
function updateModeInfo(mode) {
    // Create a user-friendly label based on the selected mode
    let label = mode === '8-queens' ? '8 Reines' : 'Custom';

    // This function should update some element on the page to show information
    // about the selected mode. Currently it only creates the label but doesn't use it.
    // Example implementation:
    // document.getElementById('mode-info-text').textContent = 
    //    'Mode sélectionné: ' + label;
}
