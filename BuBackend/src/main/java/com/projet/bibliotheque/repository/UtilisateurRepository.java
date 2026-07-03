package com.projet.bibliotheque.repository;

import com.projet.bibliotheque.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRole(Utilisateur.Role role);
}
