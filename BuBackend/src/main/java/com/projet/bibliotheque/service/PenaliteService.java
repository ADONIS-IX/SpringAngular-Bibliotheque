package com.projet.bibliotheque.service;

import com.projet.bibliotheque.dto.PenaliteDto;
import com.projet.bibliotheque.exception.ConflictException;
import com.projet.bibliotheque.exception.ResourceNotFoundException;
import com.projet.bibliotheque.model.Penalite;
import com.projet.bibliotheque.repository.PenaliteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PenaliteService {

    private final PenaliteRepository penaliteRepository;
    private final DtoMapper mapper;

    public PenaliteService(PenaliteRepository penaliteRepository, DtoMapper mapper) {
        this.penaliteRepository = penaliteRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<PenaliteDto> mesPenalites(Long utilisateurId) {
        return penaliteRepository.findByEmpruntUtilisateurIdOrderByDateCreationDesc(utilisateurId)
                .stream().map(mapper::toPenaliteDto).toList();
    }

    @Transactional(readOnly = true)
    public List<PenaliteDto> toutes() {
        return penaliteRepository.findAllByOrderByDateCreationDesc()
                .stream().map(mapper::toPenaliteDto).toList();
    }

    /** Enregistrer le paiement d'une pénalité (action bibliothécaire). */
    public PenaliteDto marquerPayee(Long penaliteId) {
        Penalite p = penaliteRepository.findById(penaliteId)
                .orElseThrow(() -> new ResourceNotFoundException("Pénalité introuvable : " + penaliteId));
        if (p.getStatut() == Penalite.Statut.PAYEE) {
            throw new ConflictException("Cette pénalité est déjà payée");
        }
        p.setStatut(Penalite.Statut.PAYEE);
        return mapper.toPenaliteDto(penaliteRepository.save(p));
    }
}
