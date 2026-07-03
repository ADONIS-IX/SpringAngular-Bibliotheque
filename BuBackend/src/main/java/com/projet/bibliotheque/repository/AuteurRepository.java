package com.projet.bibliotheque.repository;

import com.projet.bibliotheque.model.Auteur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuteurRepository extends JpaRepository<Auteur, Long> {

    List<Auteur> findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(String nom, String prenom);

    Optional<Auteur> findByNomAndPrenom(String nom, String prenom);
}
