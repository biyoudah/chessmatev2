/**
 * ChessMate Game Interface Script
 * This script handles the interactive chess game interface, including piece placement,
 * board interaction, game mode selection, and communication with the server.
 */

/**
 * Global application state
 * Manages the current state of the game interface with localStorage persistence
 * for user preferences across sessions.
 */
let appState = {
    piece: localStorage.getItem('chessPiece') || 'dame',  // Selected chess piece type
    isWhite: localStorage.getItem('chessColor') !== 'false',  // Selected piece color (white/black)
    mode: localStorage.getItem('chessMode') || 'place',  // Current interaction mode (place/remove)
    isLoading: false  // Flag to prevent multiple simultaneous requests
};

/**
 * Initialize the game interface when the DOM is fully loaded
 */
document.addEventListener("DOMContentLoaded", () => {
    // Update the UI to reflect the current state
    updateVisuals();
    // Check for any server messages that need to be displayed
    checkServerMessage();
});

/**
 * ===================================
 * UI INTERACTION FUNCTIONS
 * ===================================
 */

/**
 * Selects a chess piece type when a piece button is clicked
 * 
 * @param {HTMLElement} btn - The button element that was clicked
 */
function selectPiece(btn) {
    // Update the selected piece in the application state
    appState.piece = btn.getAttribute('data-piece');
    // Save the selection to localStorage for persistence
    localStorage.setItem('chessPiece', appState.piece);
    // Automatically switch to 'place' mode when selecting a piece
    setModePlace();
}

/**
 * Selects a piece color (white or black) when a color button is clicked
 * 
 * @param {HTMLElement} btn - The button element that was clicked
 */
function selectColor(btn) {
    // Update the selected color in the application state
    appState.isWhite = btn.getAttribute('data-color') === 'true';
    // Save the selection to localStorage for persistence
    localStorage.setItem('chessColor', appState.isWhite);
    // Update the UI to reflect the new color selection
    updateVisuals();
}

/**
 * Sets the interaction mode to 'place' (for placing pieces on the board)
 */
function setModePlace() {
    // Update the mode in the application state
    appState.mode = 'place';
    // Save the mode to localStorage for persistence
    localStorage.setItem('chessMode', 'place');
    // Update the UI to reflect the new mode
    updateVisuals();
}

/**
 * Sets the interaction mode to 'remove' (for removing pieces from the board)
 */
function setModeRemove() {
    // Update the mode in the application state
    appState.mode = 'remove';
    // Save the mode to localStorage for persistence
    localStorage.setItem('chessMode', 'remove');
    // Update the UI to reflect the new mode
    updateVisuals();
}

/**
 * Updates the visual state of UI elements based on the current application state
 * This includes highlighting selected pieces, colors, and modes
 */
function updateVisuals() {
    // Update piece selection buttons
    document.querySelectorAll('.piece-btn').forEach(btn => {
        if (btn.getAttribute('data-piece') === appState.piece) btn.classList.add('selected');
        else btn.classList.remove('selected');
    });

    // Update color selection buttons
    document.querySelectorAll('.color-btn').forEach(btn => {
        if ((btn.getAttribute('data-color') === 'true') === appState.isWhite) btn.classList.add('selected');
        else btn.classList.remove('selected');
    });

    // Update mode selection buttons
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
 * ===================================
 * BOARD INTERACTION & SERVER COMMUNICATION
 * ===================================
 */

/**
 * Handles a click on a chess board square
 * Sends the appropriate request to the server based on the current mode
 * 
 * @param {HTMLElement} caseElem - The board square element that was clicked
 */
async function clickCase(caseElem) {
    // Prevent multiple simultaneous requests
    if (appState.isLoading) return;

    // Get the coordinates of the clicked square
    const x = caseElem.getAttribute('data-x');
    const y = caseElem.getAttribute('data-y');
    const csrfToken = document.querySelector('input[name="_csrf"]').value;

    // Prepare the form data to send to the server
    const formData = new FormData();
    formData.append('_csrf', csrfToken);
    formData.append('x', x);
    formData.append('y', y);

    // Determine the endpoint based on the current mode
    let url = (appState.mode === 'place') ? '/place' : '/remove';

    // Add additional data for piece placement
    if (appState.mode === 'place') {
        formData.append('pieceType', appState.piece);
        formData.append('estBlanc', appState.isWhite);
    }

    // Set loading state and visual feedback
    appState.isLoading = true;
    caseElem.style.opacity = '0.5';

    try {
        // Send the request to the server
        const response = await fetch(url, { method: 'POST', body: formData });
        if (response.ok) await updatePageContent(response);
    } catch (e) { 
        console.error('Error during board interaction:', e); 
    } finally {
        // Reset loading state and visual feedback
        appState.isLoading = false;
        caseElem.style.opacity = '1';
    }
}

/**
 * ===================================
 * GAME MODE MANAGEMENT
 * ===================================
 */

/**
 * Changes the game mode based on the selected option
 * 
 * @param {HTMLSelectElement} selectElem - The select element containing the mode options
 */
async function changeMode(selectElem) {
    const val = selectElem.value;

    // Handle custom mode selection
    if (val === 'custom') {
        document.getElementById('customConfigPanel').style.display = 'block';
        selectElem.disabled = true;
        return;
    }

    // Prepare the form data for mode change
    const csrfToken = document.querySelector('input[name="_csrf"]').value;
    const formData = new FormData();
    formData.append('_csrf', csrfToken);
    formData.append('modeDeJeu', val);

    // Visual feedback during mode change
    document.querySelector('.container').style.opacity = '0.6';

    try {
        // Send the mode change request to the server
        const response = await fetch('/changeMode', { method: 'POST', body: formData });
        if (response.ok) await updatePageContent(response);
    } catch (e) { 
        console.error('Error changing game mode:', e); 
    } finally { 
        // Reset visual feedback
        document.querySelector('.container').style.opacity = '1'; 
    }
}

/**
 * Submits custom game configuration to the server
 */
async function submitCustomConfig() {
    // Prepare the form data with custom configuration
    const csrfToken = document.querySelector('input[name="_csrf"]').value;
    const formData = new FormData();
    formData.append('_csrf', csrfToken);

    // Add each piece configuration value
    // IMPORTANT: The field names must match what the server controller expects
    formData.append('dame', document.getElementById('c_dame').value);
    formData.append('tour', document.getElementById('c_tour').value);
    formData.append('fou', document.getElementById('c_fou').value);
    formData.append('cavalier', document.getElementById('c_cavalier').value);
    formData.append('roi', document.getElementById('c_roi').value);

    try {
        // Send the custom configuration to the server
        const response = await fetch('/customConfig', { method: 'POST', body: formData });
        if (response.ok) {
            await updatePageContent(response);

            // Check if there was an error in the response
            const errorMsg = document.getElementById('server-data-message');

            // If there's an error, keep the panel open for correction
            if (errorMsg && errorMsg.dataset.type === 'error') {
                // Do nothing, user needs to correct the input
            } else {
                // Success: close the custom configuration panel
                cancelCustom();
            }
        }
    } catch (e) { 
        console.error('Error submitting custom configuration:', e); 
    }
}

/**
 * Cancels custom mode configuration and closes the panel
 */
function cancelCustom() {
    // Hide the custom configuration panel
    document.getElementById('customConfigPanel').style.display = 'none';
    // Re-enable the mode selection dropdown
    const select = document.getElementById('modeSelect');
    select.disabled = false;
    // Note: The select will visually remain on "Custom" if the server validated the change
}

/**
 * ===================================
 * DOM UPDATES & NOTIFICATIONS
 * ===================================
 */

/**
 * Updates the page content based on the server response
 * 
 * @param {Response} response - The fetch response from the server
 */
async function updatePageContent(response) {
    // Parse the HTML response
    const html = await response.text();
    const doc = new DOMParser().parseFromString(html, 'text/html');

    // 1. Replace the chess board
    document.querySelector('.board-container').innerHTML = doc.querySelector('.board-container').innerHTML;

    // 2. Replace the info panel (contains objectives and mode selector)
    document.querySelector('.info-panel').innerHTML = doc.querySelector('.info-panel').innerHTML;

    // 3. Process any server messages
    const messageData = doc.getElementById('server-data-message');
    if (messageData) {
        showToast(messageData.dataset.msg, messageData.dataset.type);
    }

    // 4. Restore the visual state (selected buttons) after HTML changes
    updateVisuals();
}

/**
 * Checks for server messages on page load and displays them as toasts
 */
function checkServerMessage() {
    const messageData = document.getElementById('server-data-message');
    if (messageData) {
        showToast(messageData.dataset.msg, messageData.dataset.type);
        messageData.remove();
    }
}

/**
 * ===================================
 * TOAST NOTIFICATION SYSTEM
 * ===================================
 */

/**
 * Displays a toast notification message
 * 
 * @param {string} message - The message to display
 * @param {string} type - The type of message (success, error, info, etc.)
 */
function showToast(message, type) {
    if (!message) return;

    // Create and append the toast element
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerText = message;
    container.appendChild(toast);

    // Automatically remove the toast after a delay
    setTimeout(() => { toast.remove(); }, 3500);
}
