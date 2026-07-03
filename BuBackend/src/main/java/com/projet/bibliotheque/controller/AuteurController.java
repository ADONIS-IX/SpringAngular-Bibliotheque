package com.projet.bibliotheque.controller;

import com.projet.bibliotheque.dto.AuteurDto;
import com.projet.bibliotheque.dto.AuteurRequest;
import com.projet.bibliotheque.dto.LivreDto;
import com.projet.bibliotheque.service.AuteurService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auteurs")
public class AuteurController {

    private final AuteurService auteurService;

    public AuteurController(AuteurService auteurService) {
        this.auteurService = auteurService;
    }

    @GetMapping
    public ResponseEntity<List<AuteurDto>> listerTous() {
        return ResponseEntity.ok(auteurService.listerTous());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuteurDto> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(auteurService.trouverParId(id));
    }

    @GetMapping("/{id}/livres")
    public ResponseEntity<List<LivreDto>> livres(@PathVariable Long id) {
        return ResponseEntity.ok(auteurService.livresDeLAuteur(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BIBLIOTHECAIRE','ADMIN')")
    public ResponseEntity<AuteurDto> creer(@Valid @RequestBody AuteurRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(auteurService.creer(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('BIBLIOTHECAIRE','ADMIN')")
    public ResponseEntity<AuteurDto> modifier(@PathVariable Long id, @Valid @RequestBody AuteurRequest req) {
        return ResponseEntity.ok(auteurService.modifier(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('BIBLIOTHECAIRE','ADMIN')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        auteurService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
