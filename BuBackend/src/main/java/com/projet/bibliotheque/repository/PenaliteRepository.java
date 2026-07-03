package com.projet.bibliotheque.repository;

import com.projet.bibliotheque.model.Penalite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface PenaliteRepository extends JpaRepository<Penalite, Long> {

    List<Penalite> findByEmpruntUtilisateurIdOrderByDateCreationDesc(Long utilisateurId);

    List<Penalite> findAllByOrderByDateCreationDesc();

    boolean existsByEmpruntUtilisateurIdAndStatut(Long utilisateurId, Penalite.Statut statut);

    boolean existsByEmpruntId(Long empruntId);

    java.util.Optional<Penalite> findByEmpruntId(Long empruntId);

    @Query("SELECT COALESCE(SUM(p.montant), 0) FROM Penalite p WHERE p.statut = :statut")
    BigDecimal totalParStatut(Penalite.Statut statut);
}
