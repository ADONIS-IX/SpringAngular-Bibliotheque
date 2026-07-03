package com.projet.bibliotheque.dto;

import java.time.LocalDate;

public record UtilisateurDto(
        Long id,
        String nom,
        String email,
        String role,
        boolean actif,
        LocalDate dateInscription
) {
}
