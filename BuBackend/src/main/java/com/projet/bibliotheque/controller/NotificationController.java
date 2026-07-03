package com.projet.bibliotheque.controller;

import com.projet.bibliotheque.dto.NotificationDto;
import com.projet.bibliotheque.security.CurrentUser;
import com.projet.bibliotheque.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentUser currentUser;

    public NotificationController(NotificationService notificationService, CurrentUser currentUser) {
        this.notificationService = notificationService;
        this.currentUser = currentUser;
    }

    @GetMapping("/mes-notifications")
    public ResponseEntity<List<NotificationDto>> mesNotifications() {
        return ResponseEntity.ok(notificationService.mesNotifications(currentUser.id()));
    }

    @GetMapping("/non-lues/count")
    public ResponseEntity<Map<String, Long>> compteNonLues() {
        return ResponseEntity.ok(Map.of("count", notificationService.compteNonLues(currentUser.id())));
    }

    @PatchMapping("/{id}/lue")
    public ResponseEntity<Void> marquerLue(@PathVariable Long id) {
        notificationService.marquerLue(id, currentUser.id());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/tout-lu")
    public ResponseEntity<Void> marquerToutLu() {
        notificationService.marquerToutLu(currentUser.id());
        return ResponseEntity.noContent().build();
    }
}
