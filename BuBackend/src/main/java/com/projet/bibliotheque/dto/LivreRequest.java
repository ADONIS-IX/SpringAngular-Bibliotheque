package com.projet.bibliotheque.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record LivreRequest(
        @NotBlank(message = "Le titre est obligatoire")
        String titre,

        @Size(max = 13, message = "L'ISBN ne doit pas dépasser 13 caractères")
        String isbn,

        Integer anneePublication,

        @Size(max = 60)
        String categorie,

        @NotNull(message = "Le stock total est obligatoire")
        @Min(value = 0, message = "Le stock ne peut pas être négatif")
        Integer stockTotal,

        @NotNull(message = "Au moins un auteur est requis")
        @Size(min = 1, message = "Au moins un auteur est requis")
        List<Long> auteurIds,

        // Data URI base64 : ~4 Mo de texte ≈ image de ~3 Mo. Borne serveur en écho de la
        // limite client (2 Mo) pour renvoyer un 400 propre plutôt que de saturer la BD.
        @Size(max = 4_000_000, message = "Image de couverture trop volumineuse")
        String imageUrl,

        @Size(max = 2000, message = "La description ne doit pas dépasser 2000 caractères")
        String description
) {
}