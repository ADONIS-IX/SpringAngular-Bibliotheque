package com.projet.bibliotheque.dto;

import java.time.LocalDateTime;

public record NotificationDto(
        Long id,
        String message,
        String type,
        boolean lue,
        LocalDateTime dateCreation
) {
}
