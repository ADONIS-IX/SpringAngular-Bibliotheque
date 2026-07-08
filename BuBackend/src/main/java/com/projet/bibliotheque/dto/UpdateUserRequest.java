package com.projet.bibliotheque.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(
        @NotBlank(message = "Le nom est obligatoire")
        String nom,

        @Email(message = "Email invalide")
        String email,

        String password,

        @NotBlank(message = "Le rôle est obligatoire")
        String role,

        Boolean actif
) {
}
