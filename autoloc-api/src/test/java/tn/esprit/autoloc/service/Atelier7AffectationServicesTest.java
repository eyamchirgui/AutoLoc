package tn.esprit.autoloc.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.autoloc.domain.CategorieVehicule;
import tn.esprit.autoloc.domain.Client;
import tn.esprit.autoloc.domain.Contrat;
import tn.esprit.autoloc.domain.Equipement;
import tn.esprit.autoloc.domain.Maintenance;
import tn.esprit.autoloc.domain.ModePaiement;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.domain.StatutReservation;
import tn.esprit.autoloc.domain.StatutVehicule;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.dto.ContratResponse;
import tn.esprit.autoloc.dto.PaiementRequest;
import tn.esprit.autoloc.dto.ReservationResponse;
import tn.esprit.autoloc.dto.ReservationWithContratRequestDTO;
import tn.esprit.autoloc.dto.VehiculeResponse;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.mapper.ContratMapper;
import tn.esprit.autoloc.mapper.PaiementMapper;
import tn.esprit.autoloc.mapper.ReservationMapper;
import tn.esprit.autoloc.mapper.VehiculeMapper;
import tn.esprit.autoloc.repository.IClientRepository;
import tn.esprit.autoloc.repository.IContratRepository;
import tn.esprit.autoloc.repository.IEquipementRepository;
import tn.esprit.autoloc.repository.IMaintenanceRepository;
import tn.esprit.autoloc.repository.IReservationRepository;
import tn.esprit.autoloc.repository.IVehiculeRepository;

@ExtendWith(MockitoExtension.class)
class Atelier7AffectationServicesTest {

    @Mock
    private IReservationRepository reservationRepository;
    @Mock
    private IClientRepository clientRepository;
    @Mock
    private IVehiculeRepository vehiculeRepository;
    @Mock
    private IContratRepository contratRepository;
    @Mock
    private IEquipementRepository equipementRepository;
    @Mock
    private IMaintenanceRepository maintenanceRepository;

    private ReservationServiceImpl reservationService;
    private VehiculeServiceImpl vehiculeService;
    private ContratServiceImpl contratService;

    @BeforeEach
    void setUp() {
        PaiementMapper paiementMapper = new PaiementMapper();
        reservationService = new ReservationServiceImpl(
                reservationRepository,
                clientRepository,
                vehiculeRepository,
                contratRepository,
                new ReservationMapper(),
                paiementMapper);
        vehiculeService = new VehiculeServiceImpl(
                vehiculeRepository,
                equipementRepository,
                maintenanceRepository,
                new VehiculeMapper());
        contratService = new ContratServiceImpl(
                contratRepository,
                reservationRepository,
                new ContratMapper(paiementMapper));
    }

    @Test
    void affecterReservationAClient_shouldAssignClientAndReturnDto() {
        Vehicule vehicule = sampleVehicule();
        Client nouveauClient = new Client();
        nouveauClient.setIdClient(42L);
        nouveauClient.setNom("Ben Ali");
        nouveauClient.setPrenom("Sami");

        Reservation reservation = new Reservation();
        reservation.setIdReservation(10L);
        reservation.setDateDebut(LocalDate.of(2026, 12, 1));
        reservation.setDateFin(LocalDate.of(2026, 12, 5));
        reservation.setStatut(StatutReservation.EN_ATTENTE);
        reservation.setVehicule(vehicule);

        when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));
        when(clientRepository.findByNomAndPrenom("Ben Ali", "Sami")).thenReturn(List.of(nouveauClient));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservationResponse response = reservationService.affecterReservationAClient(10L, "Ben Ali", "Sami");

        assertThat(response.idReservation()).isEqualTo(10L);
        assertThat(response.clientId()).isEqualTo(42L);
    }

    @Test
    void affecterReservationAClient_shouldThrowWhenClientNotFound() {
        Reservation reservation = new Reservation();
        reservation.setIdReservation(10L);

        when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));
        when(clientRepository.findByNomAndPrenom("Inconnu", "Test")).thenReturn(List.of());

        assertThatThrownBy(() -> reservationService.affecterReservationAClient(10L, "Inconnu", "Test"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void affecterVehiculeAEquipement_shouldLinkBothSidesAndReturnDto() {
        Vehicule vehicule = sampleVehicule();
        Equipement equipement = new Equipement();
        equipement.setIdEquipement(7L);
        equipement.setLibelle("GPS");

        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule));
        when(equipementRepository.findById(7L)).thenReturn(Optional.of(equipement));
        when(vehiculeRepository.save(any(Vehicule.class))).thenAnswer(inv -> inv.getArgument(0));

        VehiculeResponse response = vehiculeService.affecterVehiculeAEquipement(1L, 7L);

        assertThat(response.idVehicule()).isEqualTo(1L);
        assertThat(response.equipementIds()).containsExactly(7L);
        assertThat(equipement.getVehicules()).contains(vehicule);
    }

    @Test
    void affecterMaintenanceAvehicule_shouldCreateMaintenanceAndUpdateVehiculeStatus() {
        Vehicule vehicule = sampleVehicule();
        when(vehiculeRepository.findByImmatriculation("123-TU-4567")).thenReturn(Optional.of(vehicule));
        when(maintenanceRepository.save(any(Maintenance.class))).thenAnswer(inv -> {
            Maintenance m = inv.getArgument(0);
            m.setIdMaintenance(99L);
            return m;
        });
        when(vehiculeRepository.save(any(Vehicule.class))).thenAnswer(inv -> inv.getArgument(0));

        VehiculeResponse response = vehiculeService.affecterMaintenanceAvehicule("Vidange complète", "123-TU-4567");

        assertThat(response.statut()).isEqualTo(StatutVehicule.MAINTENANCE);
        assertThat(vehicule.getMaintenances()).hasSize(1);
        assertThat(vehicule.getMaintenances().get(0).getDescription()).isEqualTo("Vidange complète");
        assertThat(vehicule.getMaintenances().get(0).getDateDebut()).isEqualTo(LocalDate.now());
    }

    @Test
    void affecterContratAReservation_shouldCreateContratAndComputeTotal() {
        Vehicule vehicule = sampleVehicule();
        Reservation reservation = new Reservation();
        reservation.setIdReservation(15L);
        reservation.setDateDebut(LocalDate.of(2026, 12, 1));
        reservation.setDateFin(LocalDate.of(2026, 12, 4));
        reservation.setStatut(StatutReservation.EN_ATTENTE);
        reservation.setVehicule(vehicule);

        when(reservationRepository.findByDateDebutAndDateFin(
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 4)))
                .thenReturn(List.of(reservation));
        when(contratRepository.save(any(Contrat.class))).thenAnswer(inv -> {
            Contrat c = inv.getArgument(0);
            c.setIdContrat(25L);
            return c;
        });

        ContratResponse response = contratService.affecterContratAReservation(
                LocalDate.of(2026, 11, 25),
                LocalDate.of(2026, 12, 1),
                LocalDate.of(2026, 12, 4));

        assertThat(response.idContrat()).isEqualTo(25L);
        assertThat(response.reservationId()).isEqualTo(15L);
        assertThat(response.montantTotal()).isEqualByComparingTo("360.00");
        assertThat(response.valide()).isTrue();
        assertThat(reservation.getStatut()).isEqualTo(StatutReservation.CONFIRMEE);
    }

    @Test
    void creerReservationEtContratAssocie_shouldPersistReservationAndContrat() {
        Vehicule vehicule = sampleVehicule();
        Client client = new Client();
        client.setIdClient(5L);

        ReservationWithContratRequestDTO request = new ReservationWithContratRequestDTO(
                LocalDate.of(2026, 12, 10),
                LocalDate.of(2026, 12, 12),
                null,
                1L,
                5L,
                LocalDate.of(2026, 11, 25),
                null,
                true,
                List.of(new PaiementRequest(new BigDecimal("100.00"), LocalDate.of(2026, 11, 25), ModePaiement.CARTE)));

        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule));
        when(clientRepository.findById(5L)).thenReturn(Optional.of(client));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> {
            Reservation r = inv.getArgument(0);
            r.setIdReservation(33L);
            return r;
        });
        when(contratRepository.save(any(Contrat.class))).thenAnswer(inv -> {
            Contrat c = inv.getArgument(0);
            c.setIdContrat(44L);
            return c;
        });

        ReservationResponse response = reservationService.creerReservationEtContratAssocie(request);

        assertThat(response.idReservation()).isEqualTo(33L);
        assertThat(response.vehiculeId()).isEqualTo(1L);
        assertThat(response.clientId()).isEqualTo(5L);
        assertThat(response.statut()).isEqualTo(StatutReservation.CONFIRMEE);
        verify(contratRepository).save(any(Contrat.class));
    }

    private Vehicule sampleVehicule() {
        Vehicule vehicule = new Vehicule();
        vehicule.setIdVehicule(1L);
        vehicule.setImmatriculation("123-TU-4567");
        vehicule.setMarque("Peugeot");
        vehicule.setModele("208");
        vehicule.setCategorie(CategorieVehicule.CITADINE);
        vehicule.setTarifJournalier(new BigDecimal("120.00"));
        vehicule.setStatut(StatutVehicule.DISPONIBLE);
        return vehicule;
    }
}
