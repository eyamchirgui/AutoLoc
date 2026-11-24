package tn.esprit.autoloc.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Agence;
import tn.esprit.autoloc.dto.AgenceRequest;
import tn.esprit.autoloc.dto.AgenceResponse;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.mapper.AgenceMapper;
import tn.esprit.autoloc.repository.IAgenceRepository;

@Service
@RequiredArgsConstructor
public class AgenceServiceImpl implements IAgenceService {

    private final IAgenceRepository agenceRepository;
    private final AgenceMapper agenceMapper;

    @Override
    @Transactional
    public AgenceResponse create(AgenceRequest request) {
        Agence agence = agenceMapper.toEntity(request);
        valider(agence);
        return agenceMapper.toResponse(agenceRepository.save(agence));
    }

    @Override
    @Transactional(readOnly = true)
    public AgenceResponse findById(Long id) {
        return agenceMapper.toResponse(getOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgenceResponse> findAll() {
        return agenceRepository.findAll().stream()
                .map(agenceMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public AgenceResponse update(Long id, AgenceRequest request) {
        Agence existant = getOrThrow(id);
        Agence donnees = agenceMapper.toEntity(request);
        valider(donnees);
        existant.setNom(donnees.getNom());
        existant.setVille(donnees.getVille());
        existant.setAdresse(donnees.getAdresse());
        existant.setTelephone(donnees.getTelephone());
        return agenceMapper.toResponse(agenceRepository.save(existant));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!agenceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Agence", id);
        }
        agenceRepository.deleteById(id);
    }

    private Agence getOrThrow(Long id) {
        return agenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agence", id));
    }

    private void valider(Agence agence) {
        if (agence.getNom() == null || agence.getNom().isBlank()) {
            throw new IllegalArgumentException("Le nom de l'agence est obligatoire");
        }
    }
}