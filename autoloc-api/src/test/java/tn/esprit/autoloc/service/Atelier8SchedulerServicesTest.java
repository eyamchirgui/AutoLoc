package tn.esprit.autoloc.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import tn.esprit.autoloc.AutolocApiApplication;
import tn.esprit.autoloc.domain.Client;
import tn.esprit.autoloc.domain.Contrat;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.domain.StatutReservation;
import tn.esprit.autoloc.domain.StatutVehicule;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.repository.IContratRepository;
import tn.esprit.autoloc.repository.IReservationRepository;
import tn.esprit.autoloc.repository.IVehiculeRepository;

@ExtendWith(MockitoExtension.class)
class Atelier8SchedulerServicesTest {

    @Mock
    private IContratRepository contratRepository;
    @Mock
    private IReservationRepository reservationRepository;
    @Mock
    private IVehiculeRepository vehiculeRepository;

    @InjectMocks
    private ContratSchedulerService contratSchedulerService;

    @InjectMocks
    private VehiculeSchedulerService vehiculeSchedulerService;

    @Test
    void applicationShouldEnableScheduling() {
        assertThat(AutolocApiApplication.class.isAnnotationPresent(EnableScheduling.class)).isTrue();
    }

    @Test
    void contratScheduler_shouldHaveWeekday7amCronAndQuery48HoursHorizon() throws NoSuchMethodException {
        Method method = ContratSchedulerService.class.getMethod("notifierContratsArrivantAEcheance");
        Scheduled scheduled = method.getAnnotation(Scheduled.class);
        assertThat(scheduled).isNotNull();
        assertThat(scheduled.cron()).isEqualTo("0 0 7 * * MON-FRI");

        LocalDate today = LocalDate.now();
        Client client = new Client();
        client.setNom("Trabelsi");
        client.setPrenom("Amira");

        Vehicule vehicule = new Vehicule();
        vehicule.setImmatriculation("123-TU-4567");

        Reservation reservation = new Reservation();
        reservation.setIdReservation(1L);
        reservation.setDateFin(today.plusDays(1));
        reservation.setClient(client);
        reservation.setVehicule(vehicule);

        Contrat contrat = new Contrat();
        contrat.setIdContrat(10L);
        contrat.setReservation(reservation);

        when(contratRepository.findContratsArrivantAEcheance(today, today.plusDays(2)))
                .thenReturn(List.of(contrat));

        contratSchedulerService.notifierContratsArrivantAEcheance();

        verify(contratRepository).findContratsArrivantAEcheance(today, today.plusDays(2));
    }

    @Test
    void vehiculeScheduler_shouldHaveDaily1amCronAndCloseExpiredRentals() throws NoSuchMethodException {
        Method method = VehiculeSchedulerService.class.getMethod("libererVehiculesFinLocation");
        Scheduled scheduled = method.getAnnotation(Scheduled.class);
        assertThat(scheduled).isNotNull();
        assertThat(scheduled.cron()).isEqualTo("0 0 1 * * *");

        LocalDate today = LocalDate.now();
        Vehicule vehiculeLoue = new Vehicule();
        vehiculeLoue.setIdVehicule(3L);
        vehiculeLoue.setImmatriculation("123-TU-4567");
        vehiculeLoue.setStatut(StatutVehicule.LOUE);

        Reservation reservationExpiree = new Reservation();
        reservationExpiree.setIdReservation(5L);
        reservationExpiree.setDateFin(today.minusDays(1));
        reservationExpiree.setStatut(StatutReservation.CONFIRMEE);
        reservationExpiree.setVehicule(vehiculeLoue);

        Vehicule vehiculeEnMaintenance = new Vehicule();
        vehiculeEnMaintenance.setIdVehicule(4L);
        vehiculeEnMaintenance.setImmatriculation("999-TU-1111");
        vehiculeEnMaintenance.setStatut(StatutVehicule.MAINTENANCE);

        Reservation reservationMaintenance = new Reservation();
        reservationMaintenance.setIdReservation(6L);
        reservationMaintenance.setDateFin(today.minusDays(2));
        reservationMaintenance.setStatut(StatutReservation.CONFIRMEE);
        reservationMaintenance.setVehicule(vehiculeEnMaintenance);

        when(reservationRepository.findReservationsExpirees(today))
                .thenReturn(List.of(reservationExpiree, reservationMaintenance));

        vehiculeSchedulerService.libererVehiculesFinLocation();

        assertThat(reservationExpiree.getStatut()).isEqualTo(StatutReservation.TERMINEE);
        assertThat(vehiculeLoue.getStatut()).isEqualTo(StatutVehicule.DISPONIBLE);
        assertThat(reservationMaintenance.getStatut()).isEqualTo(StatutReservation.TERMINEE);
        assertThat(vehiculeEnMaintenance.getStatut()).isEqualTo(StatutVehicule.MAINTENANCE);

        verify(reservationRepository).save(reservationExpiree);
        verify(vehiculeRepository).save(vehiculeLoue);
        verify(reservationRepository).save(reservationMaintenance);
        verify(vehiculeRepository, never()).save(vehiculeEnMaintenance);
    }
}
