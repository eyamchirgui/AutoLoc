package tn.esprit.autoloc.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Paiement;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.IPaiementRepository;
import tn.esprit.autoloc.service.IPaiementService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaiementServiceImpl implements IPaiementService {

    private final IPaiementRepository paiementRepository;

    @Override
    @Transactional
    public Paiement create(Paiement paiement) {
        paiement.setIdPaiement(null);
        return paiementRepository.save(paiement);
    }

    @Override
    @Transactional(readOnly = true)
    public Paiement getById(Long id) {
        return paiementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement introuvable : " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Paiement> getAll() {
        return paiementRepository.findAll();
    }

    @Override
    @Transactional
    public Paiement update(Long id, Paiement data) {
        Paiement existing = getById(id);
        existing.setMontant(data.getMontant());
        existing.setDatePaiement(data.getDatePaiement());
        existing.setModePaiement(data.getModePaiement());
        return paiementRepository.save(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!paiementRepository.existsById(id)) {
            throw new ResourceNotFoundException("Paiement introuvable : " + id);
        }
        paiementRepository.deleteById(id);
    }
}