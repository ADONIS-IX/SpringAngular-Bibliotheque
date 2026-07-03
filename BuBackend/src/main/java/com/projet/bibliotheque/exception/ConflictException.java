package com.projet.bibliotheque.exception;

/** Levée pour un conflit d'état métier (stock épuisé, doublon, règle violée) → HTTP 409. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
