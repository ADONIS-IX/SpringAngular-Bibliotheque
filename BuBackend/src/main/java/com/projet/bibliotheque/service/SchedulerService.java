package com.projet.bibliotheque.service;

import com.projet.bibliotheque.model.Emprunt;
import com.projet.bibliotheque.model.Notification;
import com.projet.bibliotheque.model.Reservation;
import com.projet.bibliotheque.repository.EmpruntRepository;
import com.projet.bibliotheque.repository.NotificationRepository;
import com.projet.bibliotheque.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Traitements automatiques quotidiens. La méthode {@link #executerTraitements()}
 * est aussi exposée via un endpoint pour pouvoir déclencher la démo à la demande.
 */
@Service
public class SchedulerService {

    private static final Logger log = LoggerFactory.getLogger(SchedulerService.class);

    private final EmpruntRepository empruntRepository;
    private final ReservationRepository reservationRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final ReservationService reservationService;

    public SchedulerService(EmpruntRepository empruntRepository, ReservationRepository reservationRepository,
                            NotificationRepository notificationRepository, NotificationService notificationService,
                            ReservationService reservationService) {
        this.empruntRepository = empruntRepository;
        this.reservationRepository = reservationRepository;
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
        this.reservationService = reservationService;
    }

    /** Tous les jours à 08:00. */
    @Scheduled(cron = "0 0 8 * * *")
    public void tacheQuotidienne() {
        executerTraitements();
    }

    @Transactional
    public String executerTraitements() {
        int echeances = notifierEcheancesProches();
        int retards = marquerRetards();
        int expirees = expirerReservations();
        String resume = String.format(
                "Traitements exécutés : %d notification(s) d'échéance, %d emprunt(s) passé(s) en retard, %d réservation(s) expirée(s)",
                echeances, retards, expirees);
        log.info(resume);
        return resume;
    }

    /** Notifie les emprunts EN_COURS arrivant à échéance dans les 2 jours. */
    private int notifierEcheancesProches() {
        LocalDate aujourdhui = LocalDate.now();
        List<Emprunt> proches = empruntRepository.findByStatutAndDateRetourPrevueBetween(
                Emprunt.Statut.EN_COURS, aujourdhui, aujourdhui.plusDays(2));
        int compteur = 0;
        for (Emprunt e : proches) {
            String ref = "réf. #" + e.getId();
            boolean dejaNotifie = notificationRepository.existsByUtilisateurIdAndTypeAndMessageContaining(
                    e.getUtilisateur().getId(), Notification.Type.ECHEANCE_PROCHE, ref);
            if (!dejaNotifie) {
                notificationService.creer(e.getUtilisateur(),
                        "Le livre « " + e.getLivre().getTitre() + " » est à rendre pour le "
                                + e.getDateRetourPrevue() + " (" + ref + ").",
                        Notification.Type.ECHEANCE_PROCHE);
                compteur++;
            }
        }
        return compteur;
    }

    /** Passe en EN_RETARD les emprunts non rendus dont l'échéance est dépassée. */
    private int marquerRetards() {
        List<Emprunt> depasses = empruntRepository
                .findByDateRetourEffectiveIsNullAndDateRetourPrevueBefore(LocalDate.now());
        int compteur = 0;
        for (Emprunt e : depasses) {
            if (e.getStatut() != Emprunt.Statut.EN_RETARD) {
                e.setStatut(Emprunt.Statut.EN_RETARD);
                empruntRepository.save(e);
                notificationService.creer(e.getUtilisateur(),
                        "Le livre « " + e.getLivre().getTitre() + " » est en retard depuis le "
                                + e.getDateRetourPrevue() + ". Merci de le rendre au plus vite.",
                        Notification.Type.RETARD);
                compteur++;
            }
        }
        return compteur;
    }

    /** Expire les réservations DISPONIBLE non confirmées à temps et fait avancer la file. */
    private int expirerReservations() {
        List<Reservation> expirees = reservationRepository
                .findByStatutAndDateExpirationBefore(Reservation.Statut.DISPONIBLE, LocalDateTime.now());
        int compteur = 0;
        for (Reservation r : expirees) {
            r.setStatut(Reservation.Statut.EXPIREE);
            reservationRepository.save(r);
            notificationService.creer(r.getUtilisateur(),
                    "Votre réservation du livre « " + r.getLivre().getTitre()
                            + " » a expiré (délai de retrait dépassé).",
                    Notification.Type.INFO);
            // L'exemplaire mis de côté passe au suivant, sinon retourne en stock
            reservationService.libererExemplaire(r.getLivre());
            compteur++;
        }
        return compteur;
    }
}
