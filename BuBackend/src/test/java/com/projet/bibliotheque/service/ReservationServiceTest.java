package com.projet.bibliotheque.service;

import com.projet.bibliotheque.config.BiblioProperties;
import com.projet.bibliotheque.dto.ReservationDto;
import com.projet.bibliotheque.exception.ConflictException;
import com.projet.bibliotheque.model.Livre;
import com.projet.bibliotheque.model.Notification;
import com.projet.bibliotheque.model.Reservation;
import com.projet.bibliotheque.model.Utilisateur;
import com.projet.bibliotheque.repository.LivreRepository;
import com.projet.bibliotheque.repository.ReservationRepository;
import com.projet.bibliotheque.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock ReservationRepository reservationRepository;
    @Mock LivreRepository livreRepository;
    @Mock UtilisateurRepository utilisateurRepository;
    @Mock NotificationService notificationService;

    DtoMapper mapper = new DtoMapper();
    BiblioProperties props = new BiblioProperties();

    ReservationService service;

    Utilisateur user;
    Livre livre;

    @BeforeEach
    void setUp() {
        service = new ReservationService(reservationRepository, livreRepository, utilisateurRepository,
                notificationService, mapper, props);
        user = new Utilisateur();
        user.setId(1L);
        user.setNom("Fatou Ndiaye");
        livre = new Livre();
        livre.setId(10L);
        livre.setTitre("Clean Code");
        livre.setStockDisponible(0); // épuisé → réservable
    }

    @Test
    void reserver_refuse_si_le_livre_est_disponible() {
        livre.setStockDisponible(2);
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(user));
        when(livreRepository.findById(10L)).thenReturn(Optional.of(livre));

        assertThatThrownBy(() -> service.reserver(1L, 10L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("disponible");
    }

    @Test
    void reserver_refuse_les_doublons() {
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(user));
        when(livreRepository.findById(10L)).thenReturn(Optional.of(livre));
        when(reservationRepository.existsByUtilisateurIdAndLivreIdAndStatutIn(eq(1L), eq(10L), any()))
                .thenReturn(true);

        assertThatThrownBy(() -> service.reserver(1L, 10L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("déjà une réservation");
    }

    @Test
    void reserver_calcule_la_position_dans_la_file() {
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(user));
        when(livreRepository.findById(10L)).thenReturn(Optional.of(livre));
        when(reservationRepository.existsByUtilisateurIdAndLivreIdAndStatutIn(anyLong(), anyLong(), any()))
                .thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));
        // 2 réservations plus anciennes déjà en file → position 3
        when(reservationRepository.countByLivreIdAndStatutAndDateReservationBefore(
                eq(10L), eq(Reservation.Statut.EN_ATTENTE), any())).thenReturn(2L);

        ReservationDto dto = service.reserver(1L, 10L);

        assertThat(dto.statut()).isEqualTo("EN_ATTENTE");
        assertThat(dto.position()).isEqualTo(3);
    }

    @Test
    void promouvoir_met_la_tete_de_file_a_DISPONIBLE_et_notifie() {
        Reservation tete = new Reservation();
        tete.setId(50L);
        tete.setUtilisateur(user);
        tete.setLivre(livre);
        tete.setStatut(Reservation.Statut.EN_ATTENTE);
        when(reservationRepository.findFirstByLivreIdAndStatutOrderByDateReservationAsc(
                10L, Reservation.Statut.EN_ATTENTE)).thenReturn(Optional.of(tete));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        boolean promu = service.promouvoirProchainDeLaFile(livre);

        assertThat(promu).isTrue();
        assertThat(tete.getStatut()).isEqualTo(Reservation.Statut.DISPONIBLE);
        assertThat(tete.getDateExpiration()).isNotNull();
        verify(notificationService).creer(eq(user), any(), eq(Notification.Type.RESERVATION_DISPONIBLE));
        // L'exemplaire est mis de côté : le stock N'EST PAS réincrémenté
        verify(livreRepository, never()).save(any());
    }

    @Test
    void liberer_exemplaire_sans_file_remet_en_stock() {
        when(reservationRepository.findFirstByLivreIdAndStatutOrderByDateReservationAsc(
                10L, Reservation.Statut.EN_ATTENTE)).thenReturn(Optional.empty());
        when(livreRepository.save(any(Livre.class))).thenAnswer(inv -> inv.getArgument(0));

        service.libererExemplaire(livre);

        assertThat(livre.getStockDisponible()).isEqualTo(1); // 0 → 1
        verify(livreRepository).save(livre);
    }

    @Test
    void annuler_une_reservation_disponible_libere_l_exemplaire() {
        Reservation r = new Reservation();
        r.setId(50L);
        r.setUtilisateur(user);
        r.setLivre(livre);
        r.setStatut(Reservation.Statut.DISPONIBLE);
        when(reservationRepository.findById(50L)).thenReturn(Optional.of(r));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(reservationRepository.findFirstByLivreIdAndStatutOrderByDateReservationAsc(
                10L, Reservation.Statut.EN_ATTENTE)).thenReturn(Optional.empty());
        when(livreRepository.save(any(Livre.class))).thenAnswer(inv -> inv.getArgument(0));

        service.annuler(50L, 1L);

        assertThat(r.getStatut()).isEqualTo(Reservation.Statut.ANNULEE);
        assertThat(livre.getStockDisponible()).isEqualTo(1); // exemplaire remis en stock
    }
}
