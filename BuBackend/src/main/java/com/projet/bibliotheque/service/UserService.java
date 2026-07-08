package com.projet.bibliotheque.service;

import com.projet.bibliotheque.dto.UpdateUserRequest;
import com.projet.bibliotheque.exception.ConflictException;
import com.projet.bibliotheque.exception.ResourceNotFoundException;
import com.projet.bibliotheque.model.Utilisateur;
import com.projet.bibliotheque.repository.EmpruntRepository;
import com.projet.bibliotheque.repository.NotificationRepository;
import com.projet.bibliotheque.repository.ReservationRepository;
import com.projet.bibliotheque.repository.UtilisateurRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;
    private final EmpruntRepository empruntRepository;
    private final ReservationRepository reservationRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UtilisateurRepository utilisateurRepository, EmpruntRepository empruntRepository,
                       ReservationRepository reservationRepository, NotificationRepository notificationRepository,
                       PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.empruntRepository = empruntRepository;
        this.reservationRepository = reservationRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + email));
        return User.withUsername(user.getEmail())
                .password(user.getMotDePasse())
                .roles(user.getRole().name())
                .disabled(!user.isActif())
                .build();
    }

    public Utilisateur inscrire(String nom, String email, String password) {
        if (utilisateurRepository.existsByEmail(email)) {
            throw new ConflictException("Email déjà utilisé : " + email);
        }
        Utilisateur u = new Utilisateur();
        u.setNom(nom);
        u.setEmail(email);
        u.setMotDePasse(passwordEncoder.encode(password));
        u.setRole(Utilisateur.Role.ETUDIANT);
        return utilisateurRepository.save(u);
    }

    public Utilisateur createUser(String nom, String email, String password, String role) {
        if (utilisateurRepository.existsByEmail(email)) {
            throw new ConflictException("Email déjà utilisé : " + email);
        }
        if (role != null && role.equalsIgnoreCase("ADMIN")) {
            throw new ConflictException("Impossible de créer un utilisateur avec le rôle ADMIN.");
        }
        Utilisateur u = new Utilisateur();
        u.setNom(nom);
        u.setEmail(email);
        u.setMotDePasse(passwordEncoder.encode(password));
        u.setRole(Utilisateur.Role.valueOf(role.toUpperCase()));
        return utilisateurRepository.save(u);
    }

    @Transactional(readOnly = true)
    public java.util.List<Utilisateur> lister() {
        return utilisateurRepository.findAll();
    }

    public Utilisateur updateUser(Long id, UpdateUserRequest req) {
        Utilisateur u = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + id));
        if (req.nom() != null && !req.nom().isBlank()) {
            u.setNom(req.nom());
        }
        if (req.email() != null && !req.email().isBlank() && !req.email().equalsIgnoreCase(u.getEmail())) {
            if (utilisateurRepository.existsByEmail(req.email())) {
                throw new ConflictException("Email déjà utilisé : " + req.email());
            }
            u.setEmail(req.email());
        }
        if (req.password() != null && !req.password().isBlank()) {
            u.setMotDePasse(passwordEncoder.encode(req.password()));
        }
        if (req.role() != null) {
            if (req.role().equalsIgnoreCase("ADMIN") && !u.getRole().name().equalsIgnoreCase("ADMIN")) {
                throw new ConflictException("Impossible d'assigner le rôle ADMIN.");
            }
            u.setRole(Utilisateur.Role.valueOf(req.role().toUpperCase()));
        }
        if (req.actif() != null) {
            u.setActif(req.actif());
        }
        return utilisateurRepository.save(u);
    }

    public void deleteUser(Long id) {
        Utilisateur u = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + id));
        // Un compte lié à des emprunts/réservations ne peut pas être supprimé sans casser
        // l'historique (contrainte FK) : on oriente vers la désactivation (actif = false).
        if (empruntRepository.existsByUtilisateurId(id) || reservationRepository.existsByUtilisateurId(id)) {
            throw new ConflictException("Impossible de supprimer un utilisateur ayant un historique d'emprunts "
                    + "ou de réservations. Désactivez plutôt son compte.");
        }
        // Les notifications lui appartiennent en propre : on les purge pour libérer la FK.
        notificationRepository.deleteByUtilisateurId(id);
        utilisateurRepository.delete(u);
    }

    @Transactional(readOnly = true)
    public Utilisateur parEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + email));
    }
}
