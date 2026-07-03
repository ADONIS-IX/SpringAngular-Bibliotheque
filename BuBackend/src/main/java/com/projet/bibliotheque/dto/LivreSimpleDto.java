package com.projet.bibliotheque.dto;

/** Référence légère d'un livre, utilisée dans emprunts / réservations. */
public record LivreSimpleDto(Long id, String titre, String isbn) {
}
