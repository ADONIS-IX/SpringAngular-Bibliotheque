package com.projet.bibliotheque.repository;

import com.projet.bibliotheque.model.Emprunt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EmpruntRepository extends JpaRepository<Emprunt, Long> {

    List<Emprunt> findByUtilisateurIdOrderByDateEmpruntDesc(Long utilisateurId);

    List<Emprunt> findAllByOrderByDateEmpruntDesc();

    // Emprunts non rendus d'un utilisateur (EN_COURS ou EN_RETARD)
    long countByUtilisateurIdAndDateRetourEffectiveIsNull(Long utilisateurId);

    boolean existsByUtilisateurIdAndLivreIdAndDateRetourEffectiveIsNull(Long utilisateurId, Long livreId);

    List<Emprunt> findByStatut(Emprunt.Statut statut);

    long countByStatut(Emprunt.Statut statut);

    // Emprunts dont l'échéance est dépassée et le livre pas encore rendu
    List<Emprunt> findByDateRetourEffectiveIsNullAndDateRetourPrevueBefore(LocalDate date);

    // Emprunts arrivant à échéance (pour les notifications J-2)
    List<Emprunt> findByStatutAndDateRetourPrevueBetween(Emprunt.Statut statut, LocalDate debut, LocalDate fin);

    // ── Statistiques dashboard ───────────────────────────────

    @Query("""
            SELECT l.id, l.titre, COUNT(e) AS nb FROM Emprunt e JOIN e.livre l
            GROUP BY l.id, l.titre ORDER BY nb DESC
            """)
    List<Object[]> topLivresEmpruntes(org.springframework.data.domain.Pageable pageable);

    @Query("""
            SELECT YEAR(e.dateEmprunt), MONTH(e.dateEmprunt), COUNT(e)
            FROM Emprunt e WHERE e.dateEmprunt >= :depuis
            GROUP BY YEAR(e.dateEmprunt), MONTH(e.dateEmprunt)
            ORDER BY YEAR(e.dateEmprunt), MONTH(e.dateEmprunt)
            """)
    List<Object[]> empruntsParMois(@Param("depuis") LocalDate depuis);

    @Query("SELECT e.statut, COUNT(e) FROM Emprunt e GROUP BY e.statut")
    List<Object[]> repartitionParStatut();
}
