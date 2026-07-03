package com.projet.bibliotheque.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardDto(
        // Compteurs (stat tiles)
        long totalLivres,
        long totalExemplaires,
        long exemplairesDisponibles,
        long totalMembres,
        long empruntsEnCours,
        long empruntsEnRetard,
        long reservationsEnAttente,
        long penalitesImpayees,
        BigDecimal montantPenalitesImpayees,
        // Graphiques
        List<TopLivre> topLivres,
        List<EmpruntsMois> empruntsParMois,
        List<StatutCount> repartitionStatuts
) {
    public record TopLivre(Long livreId, String titre, long nombreEmprunts) {
    }

    public record EmpruntsMois(String mois, long nombre) {
    }

    public record StatutCount(String statut, long nombre) {
    }
}
