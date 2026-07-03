package com.projet.bibliotheque.controller;

import com.projet.bibliotheque.dto.PenaliteDto;
import com.projet.bibliotheque.security.CurrentUser;
import com.projet.bibliotheque.service.PenaliteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/penalites")
public class PenaliteController {

    private final PenaliteService penaliteService;
    private final CurrentUser currentUser;

    public PenaliteController(PenaliteService penaliteService, CurrentUser currentUser) {
        this.penaliteService = penaliteService;
        this.currentUser = currentUser;
    }

    @GetMapping("/mes-penalites")
    public ResponseEntity<List<PenaliteDto>> mesPenalites() {
        return ResponseEntity.ok(penaliteService.mesPenalites(currentUser.id()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('BIBLIOTHECAIRE','ADMIN')")
    public ResponseEntity<List<PenaliteDto>> toutes() {
        return ResponseEntity.ok(penaliteService.toutes());
    }

    @PatchMapping("/{id}/payer")
    @PreAuthorize("hasAnyRole('BIBLIOTHECAIRE','ADMIN')")
    public ResponseEntity<PenaliteDto> marquerPayee(@PathVariable Long id) {
        return ResponseEntity.ok(penaliteService.marquerPayee(id));
    }
}
