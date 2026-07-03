package com.projet.bibliotheque.service;

import com.projet.bibliotheque.dto.NotificationDto;
import com.projet.bibliotheque.model.Notification;
import com.projet.bibliotheque.model.Utilisateur;
import com.projet.bibliotheque.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final DtoMapper mapper;

    public NotificationService(NotificationRepository notificationRepository, DtoMapper mapper) {
        this.notificationRepository = notificationRepository;
        this.mapper = mapper;
    }

    /** Crée une notification pour un utilisateur (utilisé par les autres services). */
    public Notification creer(Utilisateur destinataire, String message, Notification.Type type) {
        return notificationRepository.save(new Notification(destinataire, message, type));
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> mesNotifications(Long utilisateurId) {
        return notificationRepository.findByUtilisateurIdOrderByDateCreationDesc(utilisateurId)
                .stream().map(mapper::toNotificationDto).toList();
    }

    @Transactional(readOnly = true)
    public long compteNonLues(Long utilisateurId) {
        return notificationRepository.countByUtilisateurIdAndLueFalse(utilisateurId);
    }

    public void marquerLue(Long notificationId, Long utilisateurId) {
        notificationRepository.findById(notificationId)
                .filter(n -> n.getUtilisateur().getId().equals(utilisateurId))
                .ifPresent(n -> {
                    n.setLue(true);
                    notificationRepository.save(n);
                });
    }

    public void marquerToutLu(Long utilisateurId) {
        notificationRepository.marquerToutLu(utilisateurId);
    }
}
