package tn.esprit.autoloc.mapper;

import org.springframework.stereotype.Component;
import tn.esprit.autoloc.domain.Agence;
import tn.esprit.autoloc.domain.Employe;
import tn.esprit.autoloc.dto.EmployeRequest;
import tn.esprit.autoloc.dto.EmployeResponse;

@Component
public class EmployeMapper {

    public Employe toEntity(EmployeRequest request) {
        Employe employe = new Employe();
        employe.setNom(request.nom());
        employe.setPrenom(request.prenom());
        employe.setRole(request.role());
        employe.setAgence(agenceRef(request.agenceId()));
        return employe;
    }

    public EmployeResponse toResponse(Employe employe) {
        return new EmployeResponse(
                employe.getIdEmploye(),
                employe.getNom(),
                employe.getPrenom(),
                employe.getRole(),
                employe.getAgence() == null ? null : employe.getAgence().getIdAgence());
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