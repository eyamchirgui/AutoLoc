package tn.esprit.autoloc.service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Client;
import tn.esprit.autoloc.domain.Contrat;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.domain.StatutReservation;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.dto.ReservationRequest;
import tn.esprit.autoloc.dto.ReservationResponse;
import tn.esprit.autoloc.dto.ReservationWithContratRequestDTO;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.mapper.PaiementMapper;
import tn.esprit.autoloc.mapper.ReservationMapper;
import tn.esprit.autoloc.repository.IClientRepository;
import tn.esprit.autoloc.repository.IContratRepository;
import tn.esprit.autoloc.repository.IReservationRepository;
import tn.esprit.autoloc.repository.IVehiculeRepository;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements IReservationService {

    private final IReservationRepository reservationRepository;
    private final IClientRepository clientRepository;
    private final IVehiculeRepository vehiculeRepository;
    private final IContratRepository contratRepository;
    private final ReservationMapper reservationMapper;
    private final PaiementMapper paiementMapper;

    @Override
    @Transactional
    public ReservationResponse create(ReservationRequest request) {
        Reservation reservation = reservationMapper.toEntity(request);
        valider(reservation);
        if (reservation.getStatut() == null) {
            reservation.setStatut(StatutReservation.EN_ATTENTE);
        }
        return reservationMapper.toResponse(reservationRepository.save(reservation));
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse findById(Long id) {
        return reservationMapper.toResponse(getOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll().stream()
                .map(reservationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ReservationResponse update(Long id, ReservationRequest request) {
        Reservation existant = getOrThrow(id);
        Reservation donnees = reservationMapper.toEntity(request);
        valider(donnees);
        existant.setDateDebut(donnees.getDateDebut());
        existant.setDateFin(donnees.getDateFin());
        if (donnees.getStatut() != null) {
            existant.setStatut(donnees.getStatut());
        }
        existant.setVehicule(donnees.getVehicule());
        existant.setClient(donnees.getClient());
        return reservationMapper.toResponse(reservationRepository.save(existant));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Reservation", id);
        }
        reservationRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ReservationResponse affecterReservationAClient(Long idReservation, String nom, String prenom) {
        if (nom == null || nom.isBlank() || prenom == null || prenom.isBlank()) {
            throw new IllegalArgumentException("Le nom et le prénom du client sont obligatoires");
        }
        Reservation reservation = getOrThrow(idReservation);
        List<Client> clients = clientRepository.findByNomAndPrenom(nom.trim(), prenom.trim());
        if (clients.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Client introuvable avec le nom '" + nom + "' et le prénom '" + prenom + "'");
        }
        if (clients.size() > 1) {
            throw new IllegalArgumentException(
                    "Plusieurs clients existent avec le nom '" + nom + "' et le prénom '" + prenom + "'");
        }
        reservation.setClient(clients.get(0));
        return reservationMapper.toResponse(reservationRepository.save(reservation));
    }

    @Override
    @Transactional
    public ReservationResponse creerReservationEtContratAssocie(ReservationWithContratRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new IllegalArgumentException("Les données de réservation et de contrat sont obligatoires");
        }
        if (requestDTO.dateSignature() == null) {
            throw new IllegalArgumentException("La date de signature du contrat est obligatoire");
        }
        Vehicule vehicule = vehiculeRepository.findById(requestDTO.vehiculeId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule", requestDTO.vehiculeId()));
        Client client = clientRepository.findById(requestDTO.clientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", requestDTO.clientId()));

        boolean valide = requestDTO.valide() == null || requestDTO.valide();

        Reservation reservation = new Reservation();
        reservation.setDateDebut(requestDTO.dateDebut());
        reservation.setDateFin(requestDTO.dateFin());
        valider(reservation);
        reservation.setStatut(requestDTO.statut() != null
                ? requestDTO.statut()
                : (valide ? StatutReservation.CONFIRMEE : StatutReservation.EN_ATTENTE));
        reservation.setVehicule(vehicule);
        reservation.setClient(client);
        Reservation savedReservation = reservationRepository.save(reservation);

        BigDecimal montantTotal = requestDTO.montantTotal();
        if (montantTotal == null) {
            long jours = Math.max(1L, ChronoUnit.DAYS.between(
                    savedReservation.getDateDebut(), savedReservation.getDateFin()));
            montantTotal = vehicule.getTarifJournalier().multiply(BigDecimal.valueOf(jours));
        } else if (montantTotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le montant total ne peut pas être négatif");
        }

        Contrat contrat = new Contrat();
        contrat.setDateSignature(requestDTO.dateSignature());
        contrat.setMontantTotal(montantTotal);
        contrat.setValide(valide);
        contrat.setReservation(savedReservation);
        if (requestDTO.paiements() != null) {
            requestDTO.paiements()
                    .forEach(p -> contrat.addPaiement(paiementMapper.toEntity(p)));
        }
        savedReservation.setContrat(contrat);
        contratRepository.save(contrat);

        return reservationMapper.toResponse(savedReservation);
    }

    private Reservation getOrThrow(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", id));
    }

    private void valider(Reservation reservation) {
        if (reservation.getDateDebut() == null || reservation.getDateFin() == null) {
            throw new IllegalArgumentException("Les dates de début et de fin sont obligatoires");
        }
        if (reservation.getDateFin().isBefore(reservation.getDateDebut())) {
            throw new IllegalArgumentException(
                    "La date de fin ne peut pas être antérieure à la date de début");
        }
    }
}