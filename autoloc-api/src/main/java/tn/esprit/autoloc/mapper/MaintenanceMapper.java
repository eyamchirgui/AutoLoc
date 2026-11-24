package tn.esprit.autoloc.mapper;

import org.springframework.stereotype.Component;
import tn.esprit.autoloc.domain.Maintenance;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.dto.MaintenanceRequest;
import tn.esprit.autoloc.dto.MaintenanceResponse;

@Component
public class MaintenanceMapper {

    public Maintenance toEntity(MaintenanceRequest request) {
        Maintenance maintenance = new Maintenance();
        maintenance.setDateDebut(request.dateDebut());
        maintenance.setDateFin(request.dateFin());
        maintenance.setDescription(request.description());
        maintenance.setVehicule(vehiculeRef(request.vehiculeId()));
        return maintenance;
    }

    public MaintenanceResponse toResponse(Maintenance maintenance) {
        return new MaintenanceResponse(
                maintenance.getIdMaintenance(),
                maintenance.getDateDebut(),
                maintenance.getDateFin(),
                maintenance.getDescription(),
                maintenance.getVehicule() == null ? null : maintenance.getVehicule().getIdVehicule());
    }

    private Vehicule vehiculeRef(Long id) {
        if (id == null) {
            return null;
        }
        Vehicule ref = new Vehicule();
        ref.setIdVehicule(id);
        return ref;
    }
}