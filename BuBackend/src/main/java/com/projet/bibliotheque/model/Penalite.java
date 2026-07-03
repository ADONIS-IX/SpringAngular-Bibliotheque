package com.projet.bibliotheque.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Pénalité créée automatiquement au retour d'un emprunt en retard :
 * montant = joursRetard × tarif journalier (configurable).
 */
@Entity
@Table(name = "penalites")
public class Penalite {

    public enum Statut { NON_PAYEE, PAYEE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "emprunt_id", nullable = false, unique = true)
    private Emprunt emprunt;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montant;

    @Column(nullable = false)
    private int joursRetard;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut = Statut.NON_PAYEE;

    @Column(nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    public Penalite() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Emprunt getEmprunt() {
        return emprunt;
    }

    public void setEmprunt(Emprunt emprunt) {
        this.emprunt = emprunt;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public int getJoursRetard() {
        return joursRetard;
    }

    public void setJoursRetard(int joursRetard) {
        this.joursRetard = joursRetard;
    }

    public Statut getStatut() {
        return statut;
    }

    public void setStatut(Statut statut) {
        this.statut = statut;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
}
