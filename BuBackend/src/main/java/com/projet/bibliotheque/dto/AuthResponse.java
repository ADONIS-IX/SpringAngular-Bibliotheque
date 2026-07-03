package com.projet.bibliotheque.dto;

public record AuthResponse(
        String token,
        String type,
        Long userId,
        String email,
        String nom,
        String role
) {
}
