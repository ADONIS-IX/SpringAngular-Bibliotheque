package com.projet.bibliotheque.dto;

import jakarta.validation.constraints.NotNull;

public record ReservationRequest(
        @NotNull(message = "L'identifiant du livre est obligatoire")
        Long livreId
) {
}
