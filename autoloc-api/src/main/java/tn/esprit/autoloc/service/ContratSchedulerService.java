package tn.esprit.autoloc.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Contrat;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.repository.IContratRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContratSchedulerService {

    private final IContratRepository contratRepository;

    /**
     * Identifie tous les jours de la semaine (lundi-vendredi) à 7h00
     * les contrats valides dont la réservation arrive à échéance sous 48 heures.
     */
    @Scheduled(cron = "0 0 7 * * MON-FRI")
    @Transactional(readOnly = true)
    public void notifierContratsArrivantAEcheance() {
        LocalDate today = LocalDate.now();
        LocalDate horizon = today.plusDays(2);
        List<Contrat> contrats = contratRepository.findContratsArrivantAEcheance(today, horizon);
        if (contrats.isEmpty()) {
            log.info("Aucun contrat arrivant à échéance entre {} et {}", today, horizon);
            return;
        }
        for (Contrat contrat : contrats) {
            Reservation reservation = contrat.getReservation();
            log.warn("Contrat #{} arrivant à échéance le {} (réservation #{}, client {} {}, véhicule {})",
                    contrat.getIdContrat(),
                    reservation.getDateFin(),
                    reservation.getIdReservation(),
                    reservation.getClient().getNom(),
                    reservation.getClient().getPrenom(),
                    reservation.getVehicule().getImmatriculation());
        }
    }
}
