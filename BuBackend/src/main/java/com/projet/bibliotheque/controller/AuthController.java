package com.projet.bibliotheque.controller;

import com.projet.bibliotheque.dto.*;
import com.projet.bibliotheque.model.Utilisateur;
import com.projet.bibliotheque.security.CurrentUser;
import com.projet.bibliotheque.security.JwtService;
import com.projet.bibliotheque.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final UserDetailsService userDetailsService;
    private final CurrentUser currentUser;

    public AuthController(AuthenticationManager authManager, JwtService jwtService, UserService userService,
                         UserDetailsService userDetailsService, CurrentUser currentUser) {
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.currentUser = currentUser;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        userService.inscrire(req.nom(), req.email(), req.password());
        // Connexion automatique après inscription
        return ResponseEntity.status(HttpStatus.CREATED).body(authentifier(req.email(), req.password()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authentifier(req.email(), req.password()));
    }

    @GetMapping("/me")
    public ResponseEntity<UtilisateurDto> me() {
        Utilisateur u = currentUser.get();
        return ResponseEntity.ok(new UtilisateurDto(u.getId(), u.getNom(), u.getEmail(),
                u.getRole().name(), u.isActif(), u.getDateInscription()));
    }

    private AuthResponse authentifier(String email, String password) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        UserDetails details = userDetailsService.loadUserByUsername(email);
        Utilisateur u = userService.parEmail(email);
        String token = jwtService.genererToken(details, u.getRole().name());
        return new AuthResponse(token, "Bearer", u.getId(), u.getEmail(), u.getNom(), u.getRole().name());
    }
}
