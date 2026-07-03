package com.projet.bibliotheque.service;

import com.projet.bibliotheque.dto.DashboardDto;
import com.projet.bibliotheque.model.Emprunt;
import com.projet.bibliotheque.model.Penalite;
import com.projet.bibliotheque.model.Reservation;
import com.projet.bibliotheque.model.Utilisateur;
import com.projet.bibliotheque.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class StatsService {

    private final LivreRepository livreRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EmpruntRepository empruntRepository;
    private final ReservationRepository reservationRepository;
    private final PenaliteRepository penaliteRepository;

    public StatsService(LivreRepository livreRepository, UtilisateurRepository utilisateurRepository,
                        EmpruntRepository empruntRepository, ReservationRepository reservationRepository,
                        PenaliteRepository penaliteRepository) {
        this.livreRepository = livreRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.empruntRepository = empruntRepository;
        this.reservationRepository = reservationRepository;
        this.penaliteRepository = penaliteRepository;
    }

    public DashboardDto dashboard() {
        long totalExemplaires = livreRepository.findAll().stream()
                .mapToLong(l -> l.getStockTotal() == null ? 0 : l.getStockTotal()).sum();
        long exemplairesDispo = livreRepository.findAll().stream()
                .mapToLong(l -> l.getStockDisponible() == null ? 0 : l.getStockDisponible()).sum();

        long penalitesImpayees = penaliteRepository.findAllByOrderByDateCreationDesc().stream()
                .filter(p -> p.getStatut() == Penalite.Statut.NON_PAYEE).count();
        BigDecimal montantImpaye = penaliteRepository.totalParStatut(Penalite.Statut.NON_PAYEE);

        return new DashboardDto(
                livreRepository.count(),
                totalExemplaires,
                exemplairesDispo,
                utilisateurRepository.count(),
                empruntRepository.countByStatut(Emprunt.Statut.EN_COURS),
                empruntRepository.countByStatut(Emprunt.Statut.EN_RETARD),
                reservationRepository.countByStatut(Reservation.Statut.EN_ATTENTE),
                penalitesImpayees,
                montantImpaye == null ? BigDecimal.ZERO : montantImpaye,
                topLivres(),
                empruntsParMois(),
                repartitionStatuts()
        );
    }

    private List<DashboardDto.TopLivre> topLivres() {
        return empruntRepository.topLivresEmpruntes(PageRequest.of(0, 5)).stream()
                .map(r -> new DashboardDto.TopLivre(
                        (Long) r[0], (String) r[1], ((Number) r[2]).longValue()))
                .toList();
    }

    private List<DashboardDto.EmpruntsMois> empruntsParMois() {
        LocalDate depuis = LocalDate.now().minusMonths(11).withDayOfMonth(1);
        Map<YearMonth, Long> parMois = empruntRepository.empruntsParMois(depuis).stream()
                .collect(Collectors.toMap(
                        r -> YearMonth.of(((Number) r[0]).intValue(), ((Number) r[1]).intValue()),
                        r -> ((Number) r[2]).longValue()));

        List<DashboardDto.EmpruntsMois> resultat = new ArrayList<>();
        YearMonth curseur = YearMonth.from(depuis);
        YearMonth fin = YearMonth.now();
        while (!curseur.isAfter(fin)) {
            String label = curseur.getMonth().getDisplayName(TextStyle.SHORT, Locale.FRENCH)
                    + " " + curseur.getYear();
            resultat.add(new DashboardDto.EmpruntsMois(label, parMois.getOrDefault(curseur, 0L)));
            curseur = curseur.plusMonths(1);
        }
        return resultat;
    }

    private List<DashboardDto.StatutCount> repartitionStatuts() {
        return empruntRepository.repartitionParStatut().stream()
                .map(r -> new DashboardDto.StatutCount(((Emprunt.Statut) r[0]).name(), ((Number) r[1]).longValue()))
                .toList();
    }
}
