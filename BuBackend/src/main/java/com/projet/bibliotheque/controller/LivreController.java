package com.projet.bibliotheque.controller;

import com.projet.bibliotheque.dto.LivreDto;
import com.projet.bibliotheque.dto.LivreRequest;
import com.projet.bibliotheque.service.LivreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/livres")
public class LivreController {

    private final LivreService livreService;

    public LivreController(LivreService livreService) {
        this.livreService = livreService;
    }

    @GetMapping
    public ResponseEntity<List<LivreDto>> listerTous() {
        return ResponseEntity.ok(livreService.listerTous());
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<LivreDto>> disponibles() {
        return ResponseEntity.ok(livreService.listerDisponibles());
    }

    @GetMapping("/recherche")
    public ResponseEntity<List<LivreDto>> rechercher(@RequestParam String q) {
        return ResponseEntity.ok(livreService.rechercher(q));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LivreDto> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(livreService.trouverParId(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BIBLIOTHECAIRE','ADMIN')")
    public ResponseEntity<LivreDto> creer(@Valid @RequestBody LivreRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(livreService.creer(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('BIBLIOTHECAIRE','ADMIN')")
    public ResponseEntity<LivreDto> modifier(@PathVariable Long id, @Valid @RequestBody LivreRequest req) {
        return ResponseEntity.ok(livreService.modifier(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('BIBLIOTHECAIRE','ADMIN')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        livreService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
