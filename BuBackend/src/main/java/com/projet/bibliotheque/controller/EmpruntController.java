package com.projet.bibliotheque.controller;

import com.projet.bibliotheque.dto.EmpruntDto;
import com.projet.bibliotheque.dto.EmpruntRequest;
import com.projet.bibliotheque.security.CurrentUser;
import com.projet.bibliotheque.service.EmpruntService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emprunts")
public class EmpruntController {

    private final EmpruntService empruntService;
    private final CurrentUser currentUser;

    public EmpruntController(EmpruntService empruntService, CurrentUser currentUser) {
        this.empruntService = empruntService;
        this.currentUser = currentUser;
    }

    /** Emprunter un livre (au nom de l'utilisateur connecté). */
    @PostMapping
    public ResponseEntity<EmpruntDto> emprunter(@Valid @RequestBody EmpruntRequest req) {
        EmpruntDto dto = empruntService.emprunter(currentUser.id(), req.livreId());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/mes-emprunts")
    public ResponseEntity<List<EmpruntDto>> mesEmprunts() {
        return ResponseEntity.ok(empruntService.mesEmprunts(currentUser.id()));
    }

    /** Prolonger son propre emprunt. */
    @PatchMapping("/{id}/prolonger")
    public ResponseEntity<EmpruntDto> prolonger(@PathVariable Long id) {
        return ResponseEntity.ok(empruntService.prolonger(id, currentUser.id()));
    }

    // ── Espace bibliothécaire ────────────────────────────────

    @GetMapping
    @PreAuthorize("hasAnyRole('BIBLIOTHECAIRE','ADMIN')")
    public ResponseEntity<List<EmpruntDto>> tous() {
        return ResponseEntity.ok(empruntService.tous());
    }

    @GetMapping("/retards")
    @PreAuthorize("hasAnyRole('BIBLIOTHECAIRE','ADMIN')")
    public ResponseEntity<List<EmpruntDto>> retards() {
        return ResponseEntity.ok(empruntService.retards());
    }

    /** Enregistrer le retour d'un livre (guichet bibliothécaire). */
    @PatchMapping("/{id}/retour")
    @PreAuthorize("hasAnyRole('BIBLIOTHECAIRE','ADMIN')")
    public ResponseEntity<EmpruntDto> retourner(@PathVariable Long id) {
        return ResponseEntity.ok(empruntService.retourner(id));
    }
}
