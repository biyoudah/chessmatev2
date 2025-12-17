package fr.univlorraine.pierreludmannchessmate.DTO;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class InscriptionUtilisateurDTO {

    // Contrainte pour l'e-mail : ne doit pas être vide et doit suivre un format d'e-mail valide
    @Email(message = "Format d'e-mail invalide.")
    @NotBlank(message = "L'e-mail est obligatoire.")
    @Size(max = 100, message = "L'e-mail ne peut dépasser 100 caractères.")
    private String email;

    // Contrainte pour le nom d'utilisateur : obligatoire, entre 3 et 50 caractères
    // Et doit correspondre à une expression régulière (ici, seulement des lettres et chiffres)
    @NotBlank(message = "Le pseudo est obligatoire.")
    @Size(min = 3, max = 30, message = "Le pseudo doit contenir entre 3 et 30 caractères.")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Le pseudo ne doit contenir que des lettres et des chiffres.")
    private String pseudo;

    // Contrainte pour le mot de passe : obligatoire, entre 8 et 30 caractères,
    // et doit contenir au moins une majuscule, une minuscule, un chiffre et un caractère spécial.
    @NotBlank(message = "Le mot de passe est obligatoire.")
    @Size(min = 8, max = 30, message = "Le mot de passe doit contenir entre 8 et 30 caractères.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9\\s]).{8,30}$",
            message = "Le mot de passe doit contenir au moins une majuscule, une minuscule, un chiffre et un caractère spécial."
    )
    private String password;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPseudo() { return pseudo; }
    public void setPseudo(String pseudo) { this.pseudo = pseudo; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

}
