package com.projet.bibliotheque.security;

import com.projet.bibliotheque.model.Utilisateur;
import com.projet.bibliotheque.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/** Résout l'utilisateur authentifié à partir du contexte de sécurité (email → entité). */
@Component
public class CurrentUser {

    private final UserService userService;

    public CurrentUser(UserService userService) {
        this.userService = userService;
    }

    public Utilisateur get() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetails details)) {
            throw new org.springframework.security.access.AccessDeniedException("Non authentifié");
        }
        return userService.parEmail(details.getUsername());
    }

    public Long id() {
        return get().getId();
    }
}
