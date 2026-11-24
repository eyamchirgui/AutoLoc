package tn.esprit.autoloc.mapper;

import org.springframework.stereotype.Component;
import tn.esprit.autoloc.domain.Equipement;
import tn.esprit.autoloc.dto.EquipementRequest;
import tn.esprit.autoloc.dto.EquipementResponse;

@Component
public class EquipementMapper {

    public Equipement toEntity(EquipementRequest request) {
        Equipement equipement = new Equipement();
        equipement.setLibelle(request.libelle());
        return equipement;
    }

    public EquipementResponse toResponse(Equipement equipement) {
        return new EquipementResponse(
                equipement.getIdEquipement(),
                equipement.getLibelle());
    }
}