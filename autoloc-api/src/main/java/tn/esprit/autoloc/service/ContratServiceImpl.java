package tn.esprit.autoloc.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Contrat;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.domain.StatutReservation;
import tn.esprit.autoloc.dto.ContratRequest;
import tn.esprit.autoloc.dto.ContratResponse;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.mapper.ContratMapper;
import tn.esprit.autoloc.repository.IContratRepository;
import tn.esprit.autoloc.repository.IReservationRepository;

@Service
@RequiredArgsConstructor
public class ContratServiceImpl implements IContratService {

    private final IContratRepository contratRepository;
    private final IReservationRepository reservationRepository;
    private final ContratMapper contratMapper;

    @Override
    @Transactional
    public ContratResponse create(ContratRequest request) {
        Contrat contrat = contratMapper.toEntity(request);
        valider(contrat);
        return contratMapper.toResponse(contratRepository.save(contrat));
    }

    @Override
    @Transactional(readOnly = true)
    public ContratResponse findById(Long id) {
        return contratMapper.toResponse(getOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContratResponse> findAll() {
        return contratRepository.findAll().stream()
                .map(contratMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ContratResponse update(Long id, ContratRequest request) {
        Contrat existant = getOrThrow(id);
        Contrat donnees = contratMapper.toEntity(request);
        valider(donnees);
        existant.setDateSignature(donnees.getDateSignature());
        existant.setMontantTotal(donnees.getMontantTotal());
        existant.setValide(donnees.isValide());
        // ni la réservation, ni la collection de paiements ne sont modifiées ici
        return contratMapper.toResponse(contratRepository.save(existant));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!contratRepository.existsById(id)) {
            throw new ResourceNotFoundException("Contrat", id);
        }
        contratRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ContratResponse affecterContratAReservation(LocalDate dateSignature, LocalDate dateDebut, LocalDate dateFin) {
        if (dateSignature == null || dateDebut == null || dateFin == null) {
            throw new IllegalArgumentException("Les dates de signature, de début et de fin sont obligatoires");
        }
        if (dateFin.isBefore(dateDebut)) {
            throw new IllegalArgumentException("La date de fin ne peut pas être antérieure à la date de début");
        }
        List<Reservation> reservations = reservationRepository.findByDateDebutAndDateFin(dateDebut, dateFin);
        if (reservations.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Aucune réservation trouvée pour la période du " + dateDebut + " au " + dateFin);
        }
        Reservation reservation = reservations.stream()
                .filter(r -> r.getContrat() == null)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Un contrat existe déjà pour la réservation du " + dateDebut + " au " + dateFin));

        long jours = Math.max(1L, ChronoUnit.DAYS.between(reservation.getDateDebut(), reservation.getDateFin()));
        BigDecimal montantTotal = reservation.getVehicule().getTarifJournalier()
                .multiply(BigDecimal.valueOf(jours));

        Contrat contrat = new Contrat();
        contrat.setDateSignature(dateSignature);
        contrat.setMontantTotal(montantTotal);
        contrat.setValide(true);
        contrat.setReservation(reservation);
        valider(contrat);

        reservation.setContrat(contrat);
        if (reservation.getStatut() == StatutReservation.EN_ATTENTE) {
            reservation.setStatut(StatutReservation.CONFIRMEE);
        }
        return contratMapper.toResponse(contratRepository.save(contrat));
    }

    private Contrat getOrThrow(Long id) {
        return contratRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat", id));
    }

    private void valider(Contrat contrat) {
        if (contrat.getMontantTotal() == null
                || contrat.getMontantTotal().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le montant total ne peut pas être négatif");
        }
    }
}