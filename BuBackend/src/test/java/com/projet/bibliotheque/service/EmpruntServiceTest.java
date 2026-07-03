package com.projet.bibliotheque.service;

import com.projet.bibliotheque.config.BiblioProperties;
import com.projet.bibliotheque.dto.EmpruntDto;
import com.projet.bibliotheque.exception.ConflictException;
import com.projet.bibliotheque.model.*;
import com.projet.bibliotheque.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpruntServiceTest {

    @Mock EmpruntRepository empruntRepository;
    @Mock LivreRepository livreRepository;
    @Mock UtilisateurRepository utilisateurRepository;
    @Mock ReservationRepository reservationRepository;
    @Mock PenaliteRepository penaliteRepository;
    @Mock ReservationService reservationService;
    @Mock NotificationService notificationService;

    DtoMapper mapper = new DtoMapper();
    BiblioProperties props = new BiblioProperties();

    EmpruntService service;

    Utilisateur user;
    Livre livre;

    @BeforeEach
    void setUp() {
        service = new EmpruntService(empruntRepository, livreRepository, utilisateurRepository,
                reservationRepository, penaliteRepository, reservationService, notificationService, mapper, props);

        user = new Utilisateur();
        user.setId(1L);
        user.setNom("Moussa Diop");
        user.setEmail("moussa@test.sn");

        livre = new Livre();
        livre.setId(10L);
        livre.setTitre("Clean Code");
        livre.setStockTotal(1);
        livre.setStockDisponible(1);
    }

    @Test
    void emprunter_decremente_le_stock_et_fixe_l_echeance() {
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(user));
        when(livreRepository.findById(10L)).thenReturn(Optional.of(livre));
        when(empruntRepository.save(any(Emprunt.class))).thenAnswer(inv -> inv.getArgument(0));

        EmpruntDto dto = service.emprunter(1L, 10L);

        assertThat(livre.getStockDisponible()).isZero();
        assertThat(dto.dateRetourPrevue()).isEqualTo(LocalDate.now().plusDays(props.getEmprunt().getDureeJours()));
        assertThat(dto.statut()).isEqualTo("EN_COURS");
    }

    @Test
    void emprunter_refuse_si_stock_epuise() {
        livre.setStockDisponible(0);
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(user));
        when(livreRepository.findById(10L)).thenReturn(Optional.of(livre));

        assertThatThrownBy(() -> service.emprunter(1L, 10L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("réserver");
    }

    @Test
    void emprunter_refuse_si_penalite_impayee() {
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(user));
        when(livreRepository.findById(10L)).thenReturn(Optional.of(livre));
        when(penaliteRepository.existsByEmpruntUtilisateurIdAndStatut(1L, Penalite.Statut.NON_PAYEE)).thenReturn(true);

        assertThatThrownBy(() -> service.emprunter(1L, 10L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("pénalité impayée");
    }

    @Test
    void emprunter_refuse_au_dela_du_max_simultane() {
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(user));
        when(livreRepository.findById(10L)).thenReturn(Optional.of(livre));
        when(empruntRepository.countByUtilisateurIdAndDateRetourEffectiveIsNull(1L))
                .thenReturn((long) props.getEmprunt().getMaxSimultanes());

        assertThatThrownBy(() -> service.emprunter(1L, 10L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("simultanés");
    }

    @Test
    void retour_a_l_heure_ne_cree_pas_de_penalite_et_libere_l_exemplaire() {
        Emprunt emprunt = empruntEnCours(LocalDate.now().plusDays(3));
        when(empruntRepository.findById(5L)).thenReturn(Optional.of(emprunt));
        when(empruntRepository.save(any(Emprunt.class))).thenAnswer(inv -> inv.getArgument(0));

        EmpruntDto dto = service.retourner(5L);

        assertThat(dto.statut()).isEqualTo("RENDU");
        verify(penaliteRepository, never()).save(any());
        verify(reservationService).libererExemplaire(livre); // file d'attente prise en charge
    }

    @Test
    void retour_en_retard_cree_une_penalite_proportionnelle() {
        Emprunt emprunt = empruntEnCours(LocalDate.now().minusDays(4)); // 4 jours de retard
        when(empruntRepository.findById(5L)).thenReturn(Optional.of(emprunt));
        when(empruntRepository.save(any(Emprunt.class))).thenAnswer(inv -> inv.getArgument(0));
        when(penaliteRepository.existsByEmpruntId(anyLong())).thenReturn(false);

        EmpruntDto dto = service.retourner(5L);

        assertThat(dto.statut()).isEqualTo("EN_RETARD");
        ArgumentCaptor<Penalite> captor = ArgumentCaptor.forClass(Penalite.class);
        verify(penaliteRepository).save(captor.capture());
        Penalite p = captor.getValue();
        assertThat(p.getJoursRetard()).isEqualTo(4);
        assertThat(p.getMontant()).isEqualByComparingTo(BigDecimal.valueOf(400)); // 4 × 100
        verify(notificationService).creer(eq(user), any(), eq(Notification.Type.RETARD));
    }

    private Emprunt empruntEnCours(LocalDate echeance) {
        Emprunt e = new Emprunt();
        e.setId(5L);
        e.setUtilisateur(user);
        e.setLivre(livre);
        e.setDateEmprunt(LocalDate.now().minusDays(10));
        e.setDateRetourPrevue(echeance);
        e.setStatut(Emprunt.Statut.EN_COURS);
        return e;
    }
}
