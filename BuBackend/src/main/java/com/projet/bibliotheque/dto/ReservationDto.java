package com.projet.bibliotheque.dto;

import java.time.LocalDateTime;

public record ReservationDto(
        Long id,
        LivreSimpleDto livre,
        UtilisateurSimpleDto utilisateur,
        LocalDateTime dateReservation,
        String statut,
        LocalDateTime dateExpiration,
        Integer position
) {
}
