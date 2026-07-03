package com.projet.bibliotheque.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PenaliteDto(
        Long id,
        Long empruntId,
        String livreTitre,
        String utilisateurNom,
        BigDecimal montant,
        int joursRetard,
        String statut,
        LocalDateTime dateCreation
) {
}
