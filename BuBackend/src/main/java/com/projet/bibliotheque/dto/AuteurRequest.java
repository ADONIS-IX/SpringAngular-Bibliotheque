package com.projet.bibliotheque.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuteurRequest(
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 80)
        String nom,

        @NotBlank(message = "Le prénom est obligatoire")
        @Size(max = 80)
        String prenom,

        @Size(max = 60)
        String nationalite
) {
}
