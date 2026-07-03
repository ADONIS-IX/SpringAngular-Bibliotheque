package com.projet.bibliotheque.dto;

import java.time.LocalDate;

public record EmpruntDto(
        Long id,
        LivreSimpleDto livre,
        UtilisateurSimpleDto utilisateur,
        LocalDate dateEmprunt,
        LocalDate dateRetourPrevue,
        LocalDate dateRetourEffective,
        String statut,
        boolean prolonge,
        boolean enRetard,
        long joursRestants,
        PenaliteDto penalite
) {
}
