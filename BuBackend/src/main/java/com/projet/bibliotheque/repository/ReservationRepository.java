package com.projet.bibliotheque.repository;

import com.projet.bibliotheque.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // La file d'attente FIFO d'un livre
    List<Reservation> findByLivreIdAndStatutOrderByDateReservationAsc(Long livreId, Reservation.Statut statut);

    Optional<Reservation> findFirstByLivreIdAndStatutOrderByDateReservationAsc(Long livreId, Reservation.Statut statut);

    long countByLivreIdAndStatut(Long livreId, Reservation.Statut statut);

    // Position dans la file : nb de réservations en attente plus anciennes
    long countByLivreIdAndStatutAndDateReservationBefore(Long livreId, Reservation.Statut statut, LocalDateTime date);

    boolean existsByUtilisateurIdAndLivreIdAndStatutIn(Long utilisateurId, Long livreId,
                                                       Collection<Reservation.Statut> statuts);

    List<Reservation> findByUtilisateurIdOrderByDateReservationDesc(Long utilisateurId);

    List<Reservation> findAllByOrderByDateReservationDesc();

    // Réservations DISPONIBLE dont le délai de retrait est écoulé
    List<Reservation> findByStatutAndDateExpirationBefore(Reservation.Statut statut, LocalDateTime date);

    long countByStatut(Reservation.Statut statut);
}
