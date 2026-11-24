package tn.esprit.autoloc.mapper;

import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import tn.esprit.autoloc.domain.Agence;
import tn.esprit.autoloc.domain.Equipement;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.dto.VehiculeRequest;
import tn.esprit.autoloc.dto.VehiculeResponse;

@Component
public class VehiculeMapper {

    public Vehicule toEntity(VehiculeRequest request) {
        Vehicule vehicule = new Vehicule();
        vehicule.setImmatriculation(request.immatriculation());
        vehicule.setMarque(request.marque());
        vehicule.setModele(request.modele());
        vehicule.setCategorie(request.categorie());
        vehicule.setTarifJournalier(request.tarifJournalier());
        vehicule.setStatut(request.statut());
        vehicule.setAgence(agenceRef(request.agenceId()));
        return vehicule;
    }

    public VehiculeResponse toResponse(Vehicule vehicule) {
        return new VehiculeResponse(
                vehicule.getIdVehicule(),
                vehicule.getImmatriculation(),
                vehicule.getMarque(),
                vehicule.getModele(),
                vehicule.getCategorie(),
                vehicule.getTarifJournalier(),
                vehicule.getStatut(),
                vehicule.getAgence() == null ? null : vehicule.getAgence().getIdAgence(),
                vehicule.getEquipements().stream().map(Equipement::getIdEquipement).collect(Collectors.toSet()));
    }

    private Agence agenceRef(Long id) {
        if (id == null) {
            return null;
        }
        Agence ref = new Agence();
        ref.setIdAgence(id);
        return ref;
    }
}