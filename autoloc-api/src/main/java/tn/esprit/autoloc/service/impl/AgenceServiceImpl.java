package tn.esprit.autoloc.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Agence;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.IAgenceRepository;
import tn.esprit.autoloc.service.IAgenceService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgenceServiceImpl implements IAgenceService {

    private final IAgenceRepository agenceRepository;

    @Override
    @Transactional
    public Agence create(Agence agence) {
        agence.setIdAgence(null);
        return agenceRepository.save(agence);
    }

    @Override
    @Transactional(readOnly = true)
    public Agence getById(Long id) {
        return agenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agence introuvable : " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Agence> getAll() {
        return agenceRepository.findAll();
    }

    @Override
    @Transactional
    public Agence update(Long id, Agence data) {
        Agence existing = getById(id);
        existing.setNom(data.getNom());
        existing.setVille(data.getVille());
        existing.setAdresse(data.getAdresse());
        existing.setTelephone(data.getTelephone());
        return agenceRepository.save(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!agenceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Agence introuvable : " + id);
        }
        agenceRepository.deleteById(id);
    }
}