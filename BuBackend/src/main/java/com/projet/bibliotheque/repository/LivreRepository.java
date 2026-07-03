package com.projet.bibliotheque.repository;

import com.projet.bibliotheque.model.Livre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LivreRepository extends JpaRepository<Livre, Long> {

    boolean existsByIsbn(String isbn);

    List<Livre> findByStockDisponibleGreaterThan(int stock);

    // Recherche multi-critères : titre, catégorie ou nom/prénom d'un des auteurs
    @Query("""
            SELECT DISTINCT l FROM Livre l LEFT JOIN l.auteurs a
            WHERE LOWER(l.titre) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(l.categorie) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(a.nom) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(a.prenom) LIKE LOWER(CONCAT('%', :q, '%'))
            ORDER BY l.titre
            """)
    List<Livre> rechercher(@Param("q") String query);

    @Query("SELECT l FROM Livre l JOIN l.auteurs a WHERE a.id = :auteurId ORDER BY l.titre")
    List<Livre> findByAuteurId(@Param("auteurId") Long auteurId);
}
