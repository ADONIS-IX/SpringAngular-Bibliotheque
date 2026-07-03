package com.projet.bibliotheque.service;

import com.projet.bibliotheque.config.BiblioProperties;
import com.projet.bibliotheque.dto.EmpruntDto;
import com.projet.bibliotheque.exception.ConflictException;
import com.projet.bibliotheque.exception.ResourceNotFoundException;
import com.projet.bibliotheque.model.*;
import com.projet.bibliotheque.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
public class EmpruntService {

    private final EmpruntRepository empruntRepository;
    private final LivreRepository livreRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ReservationRepository reservationRepository;
    private final PenaliteRepository penaliteRepository;
    private final ReservationService reservationService;
    private final NotificationService notificationService;
    private final DtoMapper mapper;
    private final BiblioProperties props;

    public EmpruntService(EmpruntRepository empruntRepository, LivreRepository livreRepository,
                          UtilisateurRepository utilisateurRepository, ReservationRepository reservationRepository,
                          PenaliteRepository penaliteRepository, ReservationService reservationService,
                          NotificationService notificationService, DtoMapper mapper, BiblioProperties props) {
        this.empruntRepository = empruntRepository;
        this.livreRepository = livreRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.reservationRepository = reservationRepository;
        this.penaliteRepository = penaliteRepository;
        this.reservationService = reservationService;
        this.notificationService = notificationService;
        this.mapper = mapper;
        this.props = props;
    }

    /** Emprunter un livre disponible (décrémente le stock). */
    public EmpruntDto emprunter(Long utilisateurId, Long livreId) {
        Utilisateur user = getUser(utilisateurId);
        Livre livre = getLivre(livreId);

        validerEligibilite(utilisateurId, livre);
        if (livre.getStockDisponible() <= 0) {
            throw new ConflictException("Aucun exemplaire disponible pour : " + livre.getTitre()
                    + " — vous pouvez le réserver.");
        }

        livre.setStockDisponible(livre.getStockDisponible() - 1);
        livreRepository.save(livre);

        Emprunt emprunt = construireEmprunt(user, livre);
        return toDto(empruntRepository.save(emprunt));
    }

    /**
     * Confirmer une réservation devenue DISPONIBLE : crée l'emprunt sans toucher au
     * stock (l'exemplaire avait été mis de côté au moment de la mise à disposition).
     */
    public EmpruntDto confirmerReservation(Long reservationId, Long utilisateurId) {
        Reservation r = reservationService.getReservation(reservationId);
        if (!r.getUtilisateur().getId().equals(utilisateurId)) {
            throw new ConflictException("Cette réservation ne vous appartient pas");
        }
        if (r.getStatut() != Reservation.Statut.DISPONIBLE) {
            throw new ConflictException("Cette réservation n'est pas prête à être confirmée");
        }
        validerEligibilite(utilisateurId, r.getLivre());

        r.setStatut(Reservation.Statut.CONFIRMEE);
        reservationRepository.save(r);

        Emprunt emprunt = construireEmprunt(r.getUtilisateur(), r.getLivre());
        return toDto(empruntRepository.save(emprunt));
    }

    /** Retourner un livre : clôture l'emprunt, applique une pénalité si retard, libère l'exemplaire vers la file. */
    public EmpruntDto retourner(Long empruntId) {
        Emprunt emprunt = getEmprunt(empruntId);
        if (emprunt.getDateRetourEffective() != null) {
            throw new ConflictException("Ce livre a déjà été retourné");
        }

        emprunt.setDateRetourEffective(LocalDate.now());

        long joursRetard = ChronoUnit.DAYS.between(emprunt.getDateRetourPrevue(), LocalDate.now());
        if (joursRetard > 0) {
            emprunt.setStatut(Emprunt.Statut.EN_RETARD);
            creerPenalite(emprunt, (int) joursRetard);
        } else {
            emprunt.setStatut(Emprunt.Statut.RENDU);
        }
        empruntRepository.save(emprunt);

        // File d'attente : l'exemplaire va au prochain réservataire, sinon retour en stock
        reservationService.libererExemplaire(emprunt.getLivre());

        return toDto(emprunt);
    }

    /** Prolonger un emprunt d'une durée fixe, une seule fois, si aucune file d'attente. */
    public EmpruntDto prolonger(Long empruntId, Long utilisateurId) {
        Emprunt emprunt = getEmprunt(empruntId);
        if (!emprunt.getUtilisateur().getId().equals(utilisateurId)) {
            throw new ConflictException("Cet emprunt ne vous appartient pas");
        }
        if (emprunt.getDateRetourEffective() != null) {
            throw new ConflictException("Cet emprunt est déjà clôturé");
        }
        if (emprunt.isProlonge()) {
            throw new ConflictException("Cet emprunt a déjà été prolongé une fois");
        }
        if (emprunt.getStatut() == Emprunt.Statut.EN_RETARD
                || emprunt.getDateRetourPrevue().isBefore(LocalDate.now())) {
            throw new ConflictException("Un emprunt en retard ne peut pas être prolongé");
        }
        long fileAttente = reservationRepository.countByLivreIdAndStatut(
                emprunt.getLivre().getId(), Reservation.Statut.EN_ATTENTE);
        if (fileAttente > 0) {
            throw new ConflictException("Prolongation impossible : d'autres lecteurs attendent ce livre");
        }

        emprunt.setDateRetourPrevue(emprunt.getDateRetourPrevue().plusDays(props.getEmprunt().getProlongationJours()));
        emprunt.setProlonge(true);
        return toDto(empruntRepository.save(emprunt));
    }

    @Transactional(readOnly = true)
    public List<EmpruntDto> mesEmprunts(Long utilisateurId) {
        return empruntRepository.findByUtilisateurIdOrderByDateEmpruntDesc(utilisateurId)
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<EmpruntDto> tous() {
        return empruntRepository.findAllByOrderByDateEmpruntDesc().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<EmpruntDto> retards() {
        return empruntRepository.findByDateRetourEffectiveIsNullAndDateRetourPrevueBefore(LocalDate.now())
                .stream().map(this::toDto).toList();
    }

    // ── Helpers ──────────────────────────────────────────────

    private void validerEligibilite(Long utilisateurId, Livre livre) {
        if (penaliteRepository.existsByEmpruntUtilisateurIdAndStatut(utilisateurId, Penalite.Statut.NON_PAYEE)) {
            throw new ConflictException("Vous avez une pénalité impayée : régularisez-la avant d'emprunter");
        }
        if (empruntRepository.existsByUtilisateurIdAndLivreIdAndDateRetourEffectiveIsNull(utilisateurId, livre.getId())) {
            throw new ConflictException("Vous avez déjà un exemplaire de ce livre en cours d'emprunt");
        }
        long enCours = empruntRepository.countByUtilisateurIdAndDateRetourEffectiveIsNull(utilisateurId);
        if (enCours >= props.getEmprunt().getMaxSimultanes()) {
            throw new ConflictException("Limite de " + props.getEmprunt().getMaxSimultanes()
                    + " emprunts simultanés atteinte");
        }
    }

    private Emprunt construireEmprunt(Utilisateur user, Livre livre) {
        Emprunt emprunt = new Emprunt();
        emprunt.setUtilisateur(user);
        emprunt.setLivre(livre);
        emprunt.setDateEmprunt(LocalDate.now());
        emprunt.setDateRetourPrevue(LocalDate.now().plusDays(props.getEmprunt().getDureeJours()));
        emprunt.setStatut(Emprunt.Statut.EN_COURS);
        return emprunt;
    }

    private void creerPenalite(Emprunt emprunt, int joursRetard) {
        if (penaliteRepository.existsByEmpruntId(emprunt.getId())) {
            return;
        }
        BigDecimal montant = props.getPenalite().getTarifJournalier().multiply(BigDecimal.valueOf(joursRetard));
        Penalite penalite = new Penalite();
        penalite.setEmprunt(emprunt);
        penalite.setJoursRetard(joursRetard);
        penalite.setMontant(montant);
        penalite.setStatut(Penalite.Statut.NON_PAYEE);
        penaliteRepository.save(penalite);

        notificationService.creer(emprunt.getUtilisateur(),
                "Retour en retard de « " + emprunt.getLivre().getTitre() + " » (" + joursRetard
                        + " jour(s)). Pénalité de " + montant + " FCFA appliquée.",
                Notification.Type.RETARD);
    }

    private EmpruntDto toDto(Emprunt e) {
        var penalite = penaliteRepository.findByEmpruntId(e.getId())
                .map(mapper::toPenaliteDto)
                .orElse(null);
        return mapper.toEmpruntDto(e, penalite);
    }

    private Utilisateur getUser(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + id));
    }

    private Livre getLivre(Long id) {
        return livreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livre introuvable : " + id));
    }

    private Emprunt getEmprunt(Long id) {
        return empruntRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emprunt introuvable : " + id));
    }
}
