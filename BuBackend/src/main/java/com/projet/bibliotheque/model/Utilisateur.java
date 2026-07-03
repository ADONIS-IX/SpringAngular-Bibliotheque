package com.projet.bibliotheque.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "utilisateurs")
public class Utilisateur {

    public enum Role { ETUDIANT, BIBLIOTHECAIRE, ADMIN }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nom;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String motDePasse; // toujours stocké hashé (BCrypt)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.ETUDIANT;

    @Column(nullable = false)
    private boolean actif = true;

    @Column(nullable = false)
    private LocalDate dateInscription = LocalDate.now();

    public Utilisateur() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public LocalDate getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(LocalDate dateInscription) {
        this.dateInscription = dateInscription;
    }
}
