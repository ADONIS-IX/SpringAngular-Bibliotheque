package com.projet.bibliotheque.dto;

public record AuteurDto(
        Long id,
        String nom,
        String prenom,
        String nationalite,
        int nombreLivres
) {
}
