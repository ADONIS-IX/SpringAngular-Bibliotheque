package com.projet.bibliotheque.service;

import com.projet.bibliotheque.config.BiblioProperties;
import com.projet.bibliotheque.dto.ReservationDto;
import com.projet.bibliotheque.exception.ConflictException;
import com.projet.bibliotheque.exception.ResourceNotFoundException;
import com.projet.bibliotheque.model.Livre;
import com.projet.bibliotheque.model.Notification;
import com.projet.bibliotheque.model.Reservation;
import com.projet.bibliotheque.model.Utilisateur;
import com.projet.bibliotheque.repository.LivreRepository;
import com.projet.bibliotheque.repository.ReservationRepository;
import com.projet.bibliotheque.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final LivreRepository livreRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationService notificationService;
    private final DtoMapper mapper;
    private final BiblioProperties props;

    public ReservationService(ReservationRepository reservationRepository, LivreRepository livreRepository,
                              UtilisateurRepository utilisateurRepository, NotificationService notificationService,
                              DtoMapper mapper, BiblioProperties props) {
        this.reservationRepository = reservationRepository;
        this.livreRepository = livreRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.notificationService = notificationService;
        this.mapper = mapper;
        this.props = props;
    }

    /** Réserver un livre indisponible : ajoute l'utilisateur en fin de file d'attente. */
    public ReservationDto reserver(Long utilisateurId, Long livreId) {
        Utilisateur user = getUser(utilisateurId);
        Livre livre = getLivre(livreId);

        if (livre.getStockDisponible() > 0) {
            throw new ConflictException("Ce livre est disponible : empruntez-le directement plutôt que de le réserver");
        }
        boolean dejaEnFile = reservationRepository.existsByUtilisateurIdAndLivreIdAndStatutIn(
                utilisateurId, livreId, List.of(Reservation.Statut.EN_ATTENTE, Reservation.Statut.DISPONIBLE));
        if (dejaEnFile) {
            throw new ConflictException("Vous avez déjà une réservation en cours sur ce livre");
        }

        Reservation r = new Reservation();
        r.setUtilisateur(user);
        r.setLivre(livre);
        r.setDateReservation(LocalDateTime.now());
        r.setStatut(Reservation.Statut.EN_ATTENTE);
        r = reservationRepository.save(r);
        return mapper.toReservationDto(r, calculerPosition(r));
    }

    /** Annuler sa propre réservation. Si elle était DISPONIBLE, libère l'exemplaire mis de côté. */
    public void annuler(Long reservationId, Long utilisateurId) {
        Reservation r = getReservation(reservationId);
        if (!r.getUtilisateur().getId().equals(utilisateurId)) {
            throw new ConflictException("Cette réservation ne vous appartient pas");
        }
        if (r.getStatut() == Reservation.Statut.CONFIRMEE || r.getStatut() == Reservation.Statut.ANNULEE) {
            throw new ConflictException("Cette réservation ne peut plus être annulée");
        }
        boolean etaitDisponible = r.getStatut() == Reservation.Statut.DISPONIBLE;
        r.setStatut(Reservation.Statut.ANNULEE);
        reservationRepository.save(r);
        if (etaitDisponible) {
            libererExemplaire(r.getLivre());
        }
    }

    @Transactional(readOnly = true)
    public List<ReservationDto> mesReservations(Long utilisateurId) {
        return reservationRepository.findByUtilisateurIdOrderByDateReservationDesc(utilisateurId)
                .stream().map(r -> mapper.toReservationDto(r, calculerPosition(r))).toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationDto> toutes() {
        return reservationRepository.findAllByOrderByDateReservationDesc()
                .stream().map(r -> mapper.toReservationDto(r, calculerPosition(r))).toList();
    }

    // ── Logique de file d'attente réutilisée par EmpruntService et le Scheduler ──

    /**
     * Promeut la première réservation EN_ATTENTE d'un livre : elle passe DISPONIBLE,
     * l'exemplaire est mis de côté (le stock N'EST PAS réincrémenté) et l'utilisateur
     * est notifié. Retourne true si quelqu'un a été promu, false si la file est vide.
     */
    public boolean promouvoirProchainDeLaFile(Livre livre) {
        return reservationRepository
                .findFirstByLivreIdAndStatutOrderByDateReservationAsc(livre.getId(), Reservation.Statut.EN_ATTENTE)
                .map(prochaine -> {
                    prochaine.setStatut(Reservation.Statut.DISPONIBLE);
                    prochaine.setDateExpiration(LocalDateTime.now().plusHours(props.getReservation().getExpirationHeures()));
                    reservationRepository.save(prochaine);
                    notificationService.creer(prochaine.getUtilisateur(),
                            "Le livre « " + livre.getTitre() + " » que vous aviez réservé est disponible. "
                                    + "Vous avez " + props.getReservation().getExpirationHeures()
                                    + "h pour confirmer l'emprunt.",
                            Notification.Type.RESERVATION_DISPONIBLE);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Un exemplaire se libère (retour, annulation d'une résa disponible, expiration) :
     * on le donne au prochain de la file, sinon on le remet en stock.
     */
    public void libererExemplaire(Livre livre) {
        if (!promouvoirProchainDeLaFile(livre)) {
            livre.setStockDisponible(livre.getStockDisponible() + 1);
            livreRepository.save(livre);
        }
    }

    Reservation getReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation introuvable : " + id));
    }

    /** Position 1-based dans la file pour une réservation EN_ATTENTE, sinon null. */
    private Integer calculerPosition(Reservation r) {
        if (r.getStatut() != Reservation.Statut.EN_ATTENTE) {
            return null;
        }
        long avant = reservationRepository.countByLivreIdAndStatutAndDateReservationBefore(
                r.getLivre().getId(), Reservation.Statut.EN_ATTENTE, r.getDateReservation());
        return (int) avant + 1;
    }

    private Utilisateur getUser(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + id));
    }

    private Livre getLivre(Long id) {
        return livreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livre introuvable : " + id));
    }
}
