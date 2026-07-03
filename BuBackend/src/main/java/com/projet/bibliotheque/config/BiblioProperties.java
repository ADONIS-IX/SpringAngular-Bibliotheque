package com.projet.bibliotheque.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Règles métier configurables (préfixe "biblio" dans application.properties). */
@Component
@ConfigurationProperties(prefix = "biblio")
public class BiblioProperties {

    private final Emprunt emprunt = new Emprunt();
    private final Penalite penalite = new Penalite();
    private final Reservation reservation = new Reservation();

    public Emprunt getEmprunt() {
        return emprunt;
    }

    public Penalite getPenalite() {
        return penalite;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public static class Emprunt {
        private int dureeJours = 14;
        private int prolongationJours = 7;
        private int maxSimultanes = 3;

        public int getDureeJours() {
            return dureeJours;
        }

        public void setDureeJours(int dureeJours) {
            this.dureeJours = dureeJours;
        }

        public int getProlongationJours() {
            return prolongationJours;
        }

        public void setProlongationJours(int prolongationJours) {
            this.prolongationJours = prolongationJours;
        }

        public int getMaxSimultanes() {
            return maxSimultanes;
        }

        public void setMaxSimultanes(int maxSimultanes) {
            this.maxSimultanes = maxSimultanes;
        }
    }

    public static class Penalite {
        private BigDecimal tarifJournalier = new BigDecimal("100");

        public BigDecimal getTarifJournalier() {
            return tarifJournalier;
        }

        public void setTarifJournalier(BigDecimal tarifJournalier) {
            this.tarifJournalier = tarifJournalier;
        }
    }

    public static class Reservation {
        private int expirationHeures = 48;

        public int getExpirationHeures() {
            return expirationHeures;
        }

        public void setExpirationHeures(int expirationHeures) {
            this.expirationHeures = expirationHeures;
        }
    }
}
