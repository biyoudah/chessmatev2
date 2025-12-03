document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('registerForm');
    const emailInput = document.getElementById('email');
    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');

    // Événement déclenché lors de la soumission du formulaire
    form.addEventListener('submit', (event) => {
        // Empêche l'envoi du formulaire par défaut
        event.preventDefault();

        // Exécute toutes les validations
        const isEmailValid = validateEmail();
        const isUsernameValid = validateUsername();
        const isPasswordValid = validatePassword();

        // Si TOUTES les validations passent, soumettez le formulaire
        if (isEmailValid && isUsernameValid && isPasswordValid) {
            form.submit(); // Envoi du formulaire au serveur
        } else {
            // Optionnel : Afficher un message général si la validation échoue
            console.log("Validation échouée. Veuillez corriger les erreurs.");
        }
    });

    // --- Fonctions de Validation Spécifiques ---

    /**
     * Vérifie le format d'email standard.
     */
    function validateEmail() {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        const value = emailInput.value.trim();
        const errorElement = document.getElementById('emailError');

        if (!emailRegex.test(value)) {
            errorElement.textContent = "Veuillez entrer une adresse email valide.";
            emailInput.classList.add('error-input');
            return false;
        }

        errorElement.textContent = "";
        emailInput.classList.remove('error-input');
        return true;
    }

    /**
     * Vérifie le format du nom d'utilisateur (min 3 caractères, lettres et chiffres uniquement).
     */
    function validateUsername() {
        // Regex : commence (^) et termine ($) par 3 ou plus ({3,}) lettres et/ou chiffres ([a-zA-Z0-9])
        const usernameRegex = /^[a-zA-Z0-9]{3,}$/;
        const value = usernameInput.value.trim();
        const errorElement = document.getElementById('usernameError');

        if (!usernameRegex.test(value)) {
            errorElement.textContent = "Le nom d'utilisateur doit contenir au moins 3 caractères (lettres et chiffres uniquement).";
            usernameInput.classList.add('error-input');
            return false;
        }

        errorElement.textContent = "";
        usernameInput.classList.remove('error-input');
        return true;
    }

    /**
     * Vérifie la complexité du mot de passe.
     * Longueur : au moins 8 caractères.
     * Contient : 1 majuscule, 1 minuscule, 1 chiffre, 1 caractère spécial.
     */
    function validatePassword() {
        const value = passwordInput.value;
        const errorElement = document.getElementById('passwordError');
        let errorMessage = "";
        let isValid = true;

        if (value.length < 8) {
            errorMessage += "Le mot de passe doit avoir au moins 8 caractères. ";
            isValid = false;
        }
        // Lookahead: vérifie si la chaîne contient...
        if (!/(?=.*[A-Z])/.test(value)) {
            errorMessage += "Il doit contenir au moins 1 majuscule. ";
            isValid = false;
        }
        if (!/(?=.*[a-z])/.test(value)) {
            errorMessage += "Il doit contenir au moins 1 minuscule. ";
            isValid = false;
        }
        if (!/(?=.*\d)/.test(value)) {
            errorMessage += "Il doit contenir au moins 1 chiffre. ";
            isValid = false;
        }
        // Caractère spécial : tout ce qui n'est pas une lettre/chiffre/espace
        if (!/(?=.*[^a-zA-Z0-9\s])/.test(value)) {
            errorMessage += "Il doit contenir au moins 1 caractère spécial. ";
            isValid = false;
        }

        if (!isValid) {
            errorElement.textContent = errorMessage.trim();
            passwordInput.classList.add('error-input');
        } else {
            errorElement.textContent = "";
            passwordInput.classList.remove('error-input');
        }

        return isValid;
    }

    // --- Validation en temps réel (Optionnel mais recommandé) ---
    // Ajout d'écouteurs d'événements pour valider les champs à la sortie (blur) ou à la frappe (input)
    emailInput.addEventListener('blur', validateEmail);
    usernameInput.addEventListener('blur', validateUsername);
    passwordInput.addEventListener('input', validatePassword); // Valider à chaque frappe
});