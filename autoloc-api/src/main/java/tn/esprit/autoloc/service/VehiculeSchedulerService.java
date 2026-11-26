package tn.esprit.autoloc.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.domain.StatutReservation;
import tn.esprit.autoloc.domain.StatutVehicule;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.repository.IReservationRepository;
import tn.esprit.autoloc.repository.IVehiculeRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehiculeSchedulerService {

    private final IReservationRepository reservationRepository;
    private final IVehiculeRepository vehiculeRepository;

    /**
     * Clôture chaque jour à 1h00 les locations arrivées à terme et remet le véhicule à DISPONIBLE.
     */
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void libererVehiculesFinLocation() {
        LocalDate today = LocalDate.now();
        List<Reservation> reservationsExpirees = reservationRepository.findReservationsExpirees(today);
        if (reservationsExpirees.isEmpty()) {
            log.info("Aucune location expirée à clôturer au {}", today);
            return;
        }
        for (Reservation reservation : reservationsExpirees) {
            reservation.setStatut(StatutReservation.TERMINEE);
            reservationRepository.save(reservation);

            Vehicule vehicule = reservation.getVehicule();
            if (vehicule != null && vehicule.getStatut() != StatutVehicule.MAINTENANCE) {
                vehicule.setStatut(StatutVehicule.DISPONIBLE);
                vehiculeRepository.save(vehicule);
            }
            log.info("Réservation #{} clôturée (TERMINEE) - véhicule {} mis à jour (statut: {})",
                    reservation.getIdReservation(),
                    vehicule != null ? vehicule.getImmatriculation() : "N/A",
                    vehicule != null ? vehicule.getStatut() : "N/A");
        }
    }
}
