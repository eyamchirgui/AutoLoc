package tn.esprit.autoloc.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Maintenance;
import tn.esprit.autoloc.dto.MaintenanceRequest;
import tn.esprit.autoloc.dto.MaintenanceResponse;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.mapper.MaintenanceMapper;
import tn.esprit.autoloc.repository.IMaintenanceRepository;

@Service
@RequiredArgsConstructor
public class MaintenanceServiceImpl implements IMaintenanceService {

    private final IMaintenanceRepository maintenanceRepository;
    private final MaintenanceMapper maintenanceMapper;

    @Override
    @Transactional
    public MaintenanceResponse create(MaintenanceRequest request) {
        Maintenance maintenance = maintenanceMapper.toEntity(request);
        valider(maintenance);
        return maintenanceMapper.toResponse(maintenanceRepository.save(maintenance));
    }

    @Override
    @Transactional(readOnly = true)
    public MaintenanceResponse findById(Long id) {
        return maintenanceMapper.toResponse(getOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceResponse> findAll() {
        return maintenanceRepository.findAll().stream()
                .map(maintenanceMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public MaintenanceResponse update(Long id, MaintenanceRequest request) {
        Maintenance existant = getOrThrow(id);
        Maintenance donnees = maintenanceMapper.toEntity(request);
        valider(donnees);
        existant.setDateDebut(donnees.getDateDebut());
        existant.setDateFin(donnees.getDateFin());
        existant.setDescription(donnees.getDescription());
        existant.setVehicule(donnees.getVehicule());
        return maintenanceMapper.toResponse(maintenanceRepository.save(existant));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!maintenanceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Maintenance", id);
        }
        maintenanceRepository.deleteById(id);
    }

    private Maintenance getOrThrow(Long id) {
        return maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance", id));
    }

    private void valider(Maintenance maintenance) {
        if (maintenance.getDateDebut() == null) {
            throw new IllegalArgumentException("La date de début est obligatoire");
        }
        if (maintenance.getDateFin() != null
                && maintenance.getDateFin().isBefore(maintenance.getDateDebut())) {
            throw new IllegalArgumentException(
                    "La date de fin ne peut pas être antérieure à la date de début");
        }
    }
}