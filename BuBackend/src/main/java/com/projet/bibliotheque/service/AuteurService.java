package com.projet.bibliotheque.service;

import com.projet.bibliotheque.dto.AuteurDto;
import com.projet.bibliotheque.dto.AuteurRequest;
import com.projet.bibliotheque.dto.LivreDto;
import com.projet.bibliotheque.exception.ResourceNotFoundException;
import com.projet.bibliotheque.model.Auteur;
import com.projet.bibliotheque.repository.AuteurRepository;
import com.projet.bibliotheque.repository.LivreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AuteurService {

    private final AuteurRepository auteurRepository;
    private final LivreRepository livreRepository;
    private final DtoMapper mapper;

    public AuteurService(AuteurRepository auteurRepository, LivreRepository livreRepository, DtoMapper mapper) {
        this.auteurRepository = auteurRepository;
        this.livreRepository = livreRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<AuteurDto> listerTous() {
        return auteurRepository.findAll().stream().map(mapper::toAuteurDto).toList();
    }

    @Transactional(readOnly = true)
    public AuteurDto trouverParId(Long id) {
        return mapper.toAuteurDto(getAuteur(id));
    }

    @Transactional(readOnly = true)
    public List<LivreDto> livresDeLAuteur(Long id) {
        getAuteur(id); // vérifie l'existence
        return livreRepository.findByAuteurId(id).stream()
                .map(l -> mapper.toLivreDto(l, 0)).toList();
    }

    public AuteurDto creer(AuteurRequest req) {
        Auteur auteur = new Auteur(req.nom(), req.prenom(), req.nationalite());
        return mapper.toAuteurDto(auteurRepository.save(auteur));
    }

    public AuteurDto modifier(Long id, AuteurRequest req) {
        Auteur auteur = getAuteur(id);
        auteur.setNom(req.nom());
        auteur.setPrenom(req.prenom());
        auteur.setNationalite(req.nationalite());
        return mapper.toAuteurDto(auteurRepository.save(auteur));
    }

    public void supprimer(Long id) {
        Auteur auteur = getAuteur(id);
        if (!auteur.getLivres().isEmpty()) {
            throw new com.projet.bibliotheque.exception.ConflictException(
                    "Impossible de supprimer un auteur rattaché à des livres");
        }
        auteurRepository.delete(auteur);
    }

    private Auteur getAuteur(Long id) {
        return auteurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Auteur introuvable : " + id));
    }
}
