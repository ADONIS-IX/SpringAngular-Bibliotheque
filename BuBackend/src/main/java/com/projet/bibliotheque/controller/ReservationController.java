package com.projet.bibliotheque.controller;

import com.projet.bibliotheque.dto.EmpruntDto;
import com.projet.bibliotheque.dto.ReservationDto;
import com.projet.bibliotheque.dto.ReservationRequest;
import com.projet.bibliotheque.security.CurrentUser;
import com.projet.bibliotheque.service.EmpruntService;
import com.projet.bibliotheque.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final EmpruntService empruntService;
    private final CurrentUser currentUser;

    public ReservationController(ReservationService reservationService, EmpruntService empruntService,
                                 CurrentUser currentUser) {
        this.reservationService = reservationService;
        this.empruntService = empruntService;
        this.currentUser = currentUser;
    }

    /** Réserver un livre indisponible : entre dans la file d'attente. */
    @PostMapping
    public ResponseEntity<ReservationDto> reserver(@Valid @RequestBody ReservationRequest req) {
        ReservationDto dto = reservationService.reserver(currentUser.id(), req.livreId());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/mes-reservations")
    public ResponseEntity<List<ReservationDto>> mesReservations() {
        return ResponseEntity.ok(reservationService.mesReservations(currentUser.id()));
    }

    /** Confirmer l'emprunt d'une réservation devenue disponible. */
    @PostMapping("/{id}/confirmer")
    public ResponseEntity<EmpruntDto> confirmer(@PathVariable Long id) {
        EmpruntDto dto = empruntService.confirmerReservation(id, currentUser.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> annuler(@PathVariable Long id) {
        reservationService.annuler(id, currentUser.id());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('BIBLIOTHECAIRE','ADMIN')")
    public ResponseEntity<List<ReservationDto>> toutes() {
        return ResponseEntity.ok(reservationService.toutes());
    }
}
