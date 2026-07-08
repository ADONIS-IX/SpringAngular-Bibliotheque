package com.projet.bibliotheque.repository;

import com.projet.bibliotheque.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUtilisateurIdOrderByDateCreationDesc(Long utilisateurId);

    long countByUtilisateurIdAndLueFalse(Long utilisateurId);

    @Modifying
    @Query("UPDATE Notification n SET n.lue = true WHERE n.utilisateur.id = :utilisateurId AND n.lue = false")
    int marquerToutLu(@Param("utilisateurId") Long utilisateurId);

    // Anti-doublon pour le scheduler (une seule notif d'échéance par emprunt et par jour)
    boolean existsByUtilisateurIdAndTypeAndMessageContaining(Long utilisateurId, Notification.Type type, String fragment);

    // Purge des notifications d'un utilisateur (avant suppression de son compte)
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.utilisateur.id = :utilisateurId")
    int deleteByUtilisateurId(@Param("utilisateurId") Long utilisateurId);
}
