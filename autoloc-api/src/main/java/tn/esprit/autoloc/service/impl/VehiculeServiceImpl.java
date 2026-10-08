package tn.esprit.autoloc.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Agence;
import tn.esprit.autoloc.domain.StatutVehicule;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.IAgenceRepository;
import tn.esprit.autoloc.repository.IVehiculeRepository;
import tn.esprit.autoloc.service.IVehiculeService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehiculeServiceImpl implements IVehiculeService {

    private final IVehiculeRepository vehiculeRepository;
    private final IAgenceRepository agenceRepository;

    @Override
    @Transactional
    public Vehicule create(Vehicule vehicule, Long agenceId) {
        Agence agence = agenceRepository.findById(agenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Agence introuvable : " + agenceId));
        vehicule.setIdVehicule(null);
        vehicule.setAgence(agence);
        vehicule.setStatut(StatutVehicule.DISPONIBLE);
        return vehiculeRepository.save(vehicule);
    }

    @Override
    @Transactional(readOnly = true)
    public Vehicule getById(Long id) {
        return vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule introuvable : " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vehicule> getAll() {
        return vehiculeRepository.findAll();
    }

    @Override
    @Transactional
    public Vehicule update(Long id, Vehicule data) {
        Vehicule existing = getById(id);
        existing.setImmatriculation(data.getImmatriculation());
        existing.setMarque(data.getMarque());
        existing.setModele(data.getModele());
        existing.setCategorie(data.getCategorie());
        existing.setTarifJournalier(data.getTarifJournalier());
        existing.setStatut(data.getStatut());
        return vehiculeRepository.save(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!vehiculeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Véhicule introuvable : " + id);
        }
        vehiculeRepository.deleteById(id);
    }
}