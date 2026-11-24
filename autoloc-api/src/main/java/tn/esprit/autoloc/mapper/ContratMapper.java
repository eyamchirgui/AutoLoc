package tn.esprit.autoloc.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tn.esprit.autoloc.domain.Contrat;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.dto.ContratRequest;
import tn.esprit.autoloc.dto.ContratResponse;

@Component
@RequiredArgsConstructor
public class ContratMapper {

    private final PaiementMapper paiementMapper;

    public Contrat toEntity(ContratRequest request) {
        Contrat contrat = new Contrat();
        contrat.setDateSignature(request.dateSignature());
        contrat.setMontantTotal(request.montantTotal());
        contrat.setValide(request.valide());
        contrat.setReservation(reservationRef(request.reservationId()));
        if (request.paiements() != null) {
            request.paiements()
                    .forEach(p -> contrat.addPaiement(paiementMapper.toEntity(p)));
        }
        return contrat;
    }

    public ContratResponse toResponse(Contrat contrat) {
        return new ContratResponse(
                contrat.getIdContrat(),
                contrat.getDateSignature(),
                contrat.getMontantTotal(),
                contrat.isValide(),
                contrat.getReservation() == null ? null : contrat.getReservation().getIdReservation(),
                contrat.getPaiements().stream().map(paiementMapper::toResponse).toList());
    }

    private Reservation reservationRef(Long id) {
        if (id == null) {
            return null;
        }
        Reservation ref = new Reservation();
        ref.setIdReservation(id);
        return ref;
    }
}