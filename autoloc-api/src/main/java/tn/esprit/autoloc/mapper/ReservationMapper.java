package tn.esprit.autoloc.mapper;

import org.springframework.stereotype.Component;
import tn.esprit.autoloc.domain.Client;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.dto.ReservationRequest;
import tn.esprit.autoloc.dto.ReservationResponse;

@Component
public class ReservationMapper {

    public Reservation toEntity(ReservationRequest request) {
        Reservation reservation = new Reservation();
        reservation.setDateDebut(request.dateDebut());
        reservation.setDateFin(request.dateFin());
        reservation.setStatut(request.statut());
        reservation.setVehicule(vehiculeRef(request.vehiculeId()));
        reservation.setClient(clientRef(request.clientId()));
        return reservation;
    }

    public ReservationResponse toResponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.getIdReservation(),
                reservation.getDateDebut(),
                reservation.getDateFin(),
                reservation.getStatut(),
                reservation.getVehicule() == null ? null : reservation.getVehicule().getIdVehicule(),
                reservation.getClient() == null ? null : reservation.getClient().getIdClient());
    }

    private Vehicule vehiculeRef(Long id) {
        if (id == null) {
            return null;
        }
        Vehicule ref = new Vehicule();
        ref.setIdVehicule(id);
        return ref;
    }

    private Client clientRef(Long id) {
        if (id == null) {
            return null;
        }
        Client ref = new Client();
        ref.setIdClient(id);
        return ref;
    }
}