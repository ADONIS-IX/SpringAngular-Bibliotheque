package com.projet.bibliotheque.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Réservation d'un livre indisponible. La file d'attente est l'ensemble des
 * réservations EN_ATTENTE d'un livre, ordonnées par dateReservation (FIFO).
 * Au retour d'un exemplaire, la réservation en tête passe DISPONIBLE et
 * l'exemplaire est mis de côté jusqu'à dateExpiration.
 */
@Entity
@Table(name = "reservations")
public class Reservation {

    public enum Statut { EN_ATTENTE, DISPONIBLE, CONFIRMEE, ANNULEE, EXPIREE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "livre_id", nullable = false)
    private Livre livre;

    @Column(nullable = false)
    private LocalDateTime dateReservation = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut = Statut.EN_ATTENTE;

    // Renseignée quand la réservation passe DISPONIBLE
    private LocalDateTime dateExpiration;

    public Reservation() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public Livre getLivre() {
        return livre;
    }

    public void setLivre(Livre livre) {
        this.livre = livre;
    }

    public LocalDateTime getDateReservation() {
        return dateReservation;
    }

    public void setDateReservation(LocalDateTime dateReservation) {
        this.dateReservation = dateReservation;
    }

    public Statut getStatut() {
        return statut;
    }

    public void setStatut(Statut statut) {
        this.statut = statut;
    }

    public LocalDateTime getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDateTime dateExpiration) {
        this.dateExpiration = dateExpiration;
    }
}
