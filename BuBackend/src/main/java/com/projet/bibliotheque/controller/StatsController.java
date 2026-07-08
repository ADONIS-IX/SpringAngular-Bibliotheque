package com.projet.bibliotheque.controller;

import com.projet.bibliotheque.dto.DashboardDto;
import com.projet.bibliotheque.service.SchedulerService;
import com.projet.bibliotheque.service.StatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;
    private final SchedulerService schedulerService;

    public StatsController(StatsService statsService, SchedulerService schedulerService) {
        this.statsService = statsService;
        this.schedulerService = schedulerService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDto> dashboard() {
        return ResponseEntity.ok(statsService.dashboard());
    }

    /**
     * Déclenche manuellement les traitements planifiés (échéances, retards, expirations).
     * Pratique pour la démonstration sans attendre le cron quotidien.
     */
    @PostMapping("/traitements")
    @PreAuthorize("hasAnyRole('BIBLIOTHECAIRE','ADMIN')")
    public ResponseEntity<Map<String, String>> lancerTraitements() {
        return ResponseEntity.ok(Map.of("resultat", schedulerService.executerTraitements()));
    }
}
