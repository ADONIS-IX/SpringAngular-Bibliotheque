package com.projet.bibliotheque.exception;

/** Levée quand une entité demandée n'existe pas → HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
