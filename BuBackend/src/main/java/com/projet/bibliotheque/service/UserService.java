package com.projet.bibliotheque.service;

import com.projet.bibliotheque.exception.ConflictException;
import com.projet.bibliotheque.model.Utilisateur;
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
    private final PasswordEncoder passwordEncoder;

    public UserService(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
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

    @Transactional(readOnly = true)
    public Utilisateur parEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + email));
    }
}
