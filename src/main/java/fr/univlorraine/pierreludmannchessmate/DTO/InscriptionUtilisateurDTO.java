package fr.univlorraine.pierreludmannchessmate.DTO;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO utilisé pour le formulaire d'inscription.
 * <p>
 * Transporte les informations saisies par l'utilisateur sans exposer l'entité
 * {@code Utilisateur}. Les contraintes Bean Validation garantissent la
 * qualité et la sécurité des données avant création de compte.
 */
public class InscriptionUtilisateurDTO {

    @Email(message = "Format d'e-mail invalide.")
    @NotBlank(message = "L'e-mail est obligatoire.")
    @Size(max = 100, message = "L'e-mail ne peut dépasser 100 caractères.")
    /** Email de l'utilisateur. */
    private String email;

    @NotBlank(message = "Le pseudo est obligatoire.")
    @Size(min = 3, max = 30, message = "Le pseudo doit contenir entre 3 et 30 caractères.")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Le pseudo ne doit contenir que des lettres, des chiffres et des tirets bas.")
    /** Pseudo (nom d'affichage) unique souhaité par l'utilisateur. */
    private String pseudo;

    @NotBlank(message = "Le mot de passe est obligatoire.")
    @Size(min = 8, max = 30, message = "Le mot de passe doit contenir entre 8 et 30 caractères.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9\\s]).{8,30}$",
            message = "Le mot de passe doit contenir au moins une majuscule, une minuscule, un chiffre et un caractère spécial."
    )
    /** Mot de passe en clair côté formulaire (sera haché côté serveur). */
    private String password;

    /** @return l'adresse email saisie */
    public String getEmail() { return email; }
    /** @param email adresse email de contact et de connexion */
    public void setEmail(String email) { this.email = email; }
    /** @return le pseudo de l'utilisateur */
    public String getPseudo() { return pseudo; }
    /** @param pseudo pseudo souhaité, soumis à unicité et contraintes */
    public void setPseudo(String pseudo) { this.pseudo = pseudo; }
    /** @return le mot de passe saisi (non haché) */
    public String getPassword() { return password; }
    /** @param password mot de passe en clair (sera haché côté serveur) */
    public void setPassword(String password) { this.password = password; }

}
