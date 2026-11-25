package tn.esprit.autoloc.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Equipement;
import tn.esprit.autoloc.domain.Maintenance;
import tn.esprit.autoloc.domain.StatutVehicule;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.dto.VehiculeRequest;
import tn.esprit.autoloc.dto.VehiculeResponse;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.mapper.VehiculeMapper;
import tn.esprit.autoloc.repository.IEquipementRepository;
import tn.esprit.autoloc.repository.IMaintenanceRepository;
import tn.esprit.autoloc.repository.IVehiculeRepository;

@Service
@RequiredArgsConstructor
public class VehiculeServiceImpl implements IVehiculeService {

    private final IVehiculeRepository vehiculeRepository;
    private final IEquipementRepository equipementRepository;
    private final IMaintenanceRepository maintenanceRepository;
    private final VehiculeMapper vehiculeMapper;

    @Override
    @Transactional
    public VehiculeResponse create(VehiculeRequest request) {
        Vehicule vehicule = vehiculeMapper.toEntity(request);
        valider(vehicule);
        if (vehicule.getStatut() == null) {
            vehicule.setStatut(StatutVehicule.DISPONIBLE);
        }
        return vehiculeMapper.toResponse(vehiculeRepository.save(vehicule));
    }

    @Override
    @Transactional(readOnly = true)
    public VehiculeResponse findById(Long id) {
        return vehiculeMapper.toResponse(getOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculeResponse> findAll() {
        return vehiculeRepository.findAll().stream()
                .map(vehiculeMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public VehiculeResponse update(Long id, VehiculeRequest request) {
        Vehicule existant = getOrThrow(id);
        Vehicule donnees = vehiculeMapper.toEntity(request);
        valider(donnees);
        existant.setImmatriculation(donnees.getImmatriculation());
        existant.setMarque(donnees.getMarque());
        existant.setModele(donnees.getModele());
        existant.setCategorie(donnees.getCategorie());
        existant.setTarifJournalier(donnees.getTarifJournalier());
        if (donnees.getStatut() != null) {
            existant.setStatut(donnees.getStatut());
        }
        existant.setAgence(donnees.getAgence());
        return vehiculeMapper.toResponse(vehiculeRepository.save(existant));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!vehiculeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vehicule", id);
        }
        vehiculeRepository.deleteById(id);
    }

    @Override
    @Transactional
    public VehiculeResponse affecterVehiculeAEquipement(Long idVehicule, Long idEquipement) {
        Vehicule vehicule = getOrThrow(idVehicule);
        Equipement equipement = equipementRepository.findById(idEquipement)
                .orElseThrow(() -> new ResourceNotFoundException("Equipement", idEquipement));
        vehicule.getEquipements().add(equipement);
        equipement.getVehicules().add(vehicule);
        return vehiculeMapper.toResponse(vehiculeRepository.save(vehicule));
    }

    @Override
    @Transactional
    public VehiculeResponse affecterMaintenanceAvehicule(String description, String immatriculation) {
        if (immatriculation == null || immatriculation.isBlank()) {
            throw new IllegalArgumentException("L'immatriculation du véhicule est obligatoire");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("La description de la maintenance est obligatoire");
        }
        if (description.trim().length() > 255) {
            throw new IllegalArgumentException("La description ne doit pas dépasser 255 caractères");
        }
        Vehicule vehicule = vehiculeRepository.findByImmatriculation(immatriculation.trim())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vehicule introuvable avec l'immatriculation " + immatriculation));

        Maintenance maintenance = new Maintenance();
        maintenance.setDateDebut(LocalDate.now());
        maintenance.setDescription(description.trim());
        maintenance.setVehicule(vehicule);
        Maintenance savedMaintenance = maintenanceRepository.save(maintenance);

        vehicule.getMaintenances().add(savedMaintenance);
        vehicule.setStatut(StatutVehicule.MAINTENANCE);
        return vehiculeMapper.toResponse(vehiculeRepository.save(vehicule));
    }

    private Vehicule getOrThrow(Long id) {
        return vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule", id));
    }

    private void valider(Vehicule vehicule) {
        if (vehicule.getTarifJournalier() == null
                || vehicule.getTarifJournalier().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le tarif journalier ne peut pas être négatif");
        }
    }
}