package com.projet.bibliotheque.config;

import com.projet.bibliotheque.model.*;
import com.projet.bibliotheque.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Pré-remplit la base au démarrage si elle est vide. Les données sont conçues pour
 * illustrer immédiatement les fonctionnalités : livres multi-auteurs, un emprunt déjà
 * en retard, un livre épuisé avec une file d'attente de réservations.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final AuteurRepository auteurRepo;
    private final LivreRepository livreRepo;
    private final UtilisateurRepository userRepo;
    private final EmpruntRepository empruntRepo;
    private final ReservationRepository reservationRepo;
    private final PasswordEncoder encoder;

    public DataInitializer(AuteurRepository auteurRepo, LivreRepository livreRepo, UtilisateurRepository userRepo,
                           EmpruntRepository empruntRepo, ReservationRepository reservationRepo, PasswordEncoder encoder) {
        this.auteurRepo = auteurRepo;
        this.livreRepo = livreRepo;
        this.userRepo = userRepo;
        this.empruntRepo = empruntRepo;
        this.reservationRepo = reservationRepo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (userRepo.count() > 0) {
            log.info("Base déjà initialisée — aucune donnée de démo insérée.");
            return;
        }
        log.info("Initialisation des données de démonstration…");

        // Auteurs
        Auteur hugo = auteur("Hugo", "Victor", "Française");
        Auteur martin = auteur("Martin", "Robert C.", "Américaine");
        Auteur walls = auteur("Walls", "Craig", "Américaine");
        Auteur saint = auteur("Saint-Exupéry", "Antoine de", "Française");
        Auteur orwell = auteur("Orwell", "George", "Britannique");
        // Les 4 auteurs du fameux "Design Patterns" (Gang of Four) → relation N,N
        Auteur gamma = auteur("Gamma", "Erich", "Suisse");
        Auteur helm = auteur("Helm", "Richard", "Australienne");
        Auteur johnson = auteur("Johnson", "Ralph", "Américaine");
        Auteur vlissides = auteur("Vlissides", "John", "Américaine");

        // Livres
        Livre miserables = livre("Les Misérables", "9782070409228", 1862, "Roman", 3, List.of(hugo));
        Livre cleanCode = livre("Clean Code", "9780132350884", 2008, "Informatique", 1, List.of(martin));
        Livre spring = livre("Spring in Action", "9781617294945", 2018, "Informatique", 4, List.of(walls));
        Livre petitPrince = livre("Le Petit Prince", "9782070612758", 1943, "Jeunesse", 2, List.of(saint));
        Livre mille984 = livre("1984", "9780451524935", 1949, "Science-fiction", 2, List.of(orwell));
        Livre designPatterns = livre("Design Patterns", "9780201633610", 1994, "Informatique", 2,
                List.of(gamma, helm, johnson, vlissides));

        // Utilisateurs  
        utilisateur("Admin Biblio", "admin@universite.sn", "Admin2024!", Utilisateur.Role.ADMIN);
        utilisateur("Awa Bibliothécaire", "biblio@universite.sn", "Biblio2024!", Utilisateur.Role.BIBLIOTHECAIRE);
        Utilisateur etu1 = utilisateur("Moussa Diop", "etudiant@universite.sn", "Etudiant2024!", Utilisateur.Role.ETUDIANT);
        Utilisateur etu2 = utilisateur("Fatou Ndiaye", "fatou@universite.sn", "Etudiant2024!", Utilisateur.Role.ETUDIANT);
        Utilisateur etu3 = utilisateur("Cheikh Fall", "cheikh@universite.sn", "Etudiant2024!", Utilisateur.Role.ETUDIANT);

        // Emprunt EN RETARD (échéance dépassée) — statut cohérent avec la réalité de la date
        emprunt(etu1, miserables, LocalDate.now().minusDays(20), LocalDate.now().minusDays(6), Emprunt.Statut.EN_RETARD);
        decrementer(miserables);

        // Livre épuisé + file d'attente de réservations
        emprunt(etu1, cleanCode, LocalDate.now().minusDays(2), LocalDate.now().plusDays(12), Emprunt.Statut.EN_COURS);
        decrementer(cleanCode); // stock passe à 0
        reservation(etu2, cleanCode, LocalDateTime.now().minusDays(1));  // 1er de la file
        reservation(etu3, cleanCode, LocalDateTime.now().minusHours(3)); // 2e de la file

        // Historique rendu (pour les graphiques du dashboard)
        emprunt(etu2, spring, LocalDate.now().minusMonths(2).minusDays(3),
                LocalDate.now().minusMonths(2).plusDays(11), Emprunt.Statut.RENDU,
                LocalDate.now().minusMonths(2).plusDays(5));
        emprunt(etu3, mille984, LocalDate.now().minusMonths(1).minusDays(5),
                LocalDate.now().minusMonths(1).plusDays(9), Emprunt.Statut.RENDU,
                LocalDate.now().minusMonths(1).plusDays(2));
        emprunt(etu2, petitPrince, LocalDate.now().minusDays(10), LocalDate.now().plusDays(4),
                Emprunt.Statut.EN_COURS);
        decrementer(petitPrince);

        log.info("Données de démo insérées. Comptes : admin@universite.sn / biblio@universite.sn / etudiant@universite.sn");
    }

    private Auteur auteur(String nom, String prenom, String nationalite) {
        return auteurRepo.save(new Auteur(nom, prenom, nationalite));
    }

    private Livre livre(String titre, String isbn, int annee, String categorie, int stock, List<Auteur> auteurs) {
        Livre l = new Livre();
        l.setTitre(titre);
        l.setIsbn(isbn);
        l.setAnneePublication(annee);
        l.setCategorie(categorie);
        l.setStockTotal(stock);
        l.setStockDisponible(stock);
        l.setAuteurs(new java.util.ArrayList<>(auteurs));
        return livreRepo.save(l);
    }

    private Utilisateur utilisateur(String nom, String email, String motDePasse, Utilisateur.Role role) {
        Utilisateur u = new Utilisateur();
        u.setNom(nom);
        u.setEmail(email);
        u.setMotDePasse(encoder.encode(motDePasse));
        u.setRole(role);
        return userRepo.save(u);
    }

    private void emprunt(Utilisateur u, Livre l, LocalDate debut, LocalDate prevue, Emprunt.Statut statut) {
        emprunt(u, l, debut, prevue, statut, null);
    }

    private void emprunt(Utilisateur u, Livre l, LocalDate debut, LocalDate prevue, Emprunt.Statut statut,
                         LocalDate retourEffectif) {
        Emprunt e = new Emprunt();
        e.setUtilisateur(u);
        e.setLivre(l);
        e.setDateEmprunt(debut);
        e.setDateRetourPrevue(prevue);
        e.setStatut(statut);
        e.setDateRetourEffective(retourEffectif);
        empruntRepo.save(e);
    }

    private void reservation(Utilisateur u, Livre l, LocalDateTime date) {
        Reservation r = new Reservation();
        r.setUtilisateur(u);
        r.setLivre(l);
        r.setDateReservation(date);
        r.setStatut(Reservation.Statut.EN_ATTENTE);
        reservationRepo.save(r);
    }

    private void decrementer(Livre l) {
        l.setStockDisponible(l.getStockDisponible() - 1);
        livreRepo.save(l);
    }
}
