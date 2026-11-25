package tn.esprit.autoloc.service;

import java.util.List;
import tn.esprit.autoloc.dto.ReservationRequest;
import tn.esprit.autoloc.dto.ReservationResponse;
import tn.esprit.autoloc.dto.ReservationWithContratRequestDTO;

public interface IReservationService {

    ReservationResponse create(ReservationRequest request);

    ReservationResponse findById(Long id);

    List<ReservationResponse> findAll();

    ReservationResponse update(Long id, ReservationRequest request);

    void deleteById(Long id);

    ReservationResponse affecterReservationAClient(Long idReservation, String nom, String prenom);

    ReservationResponse creerReservationEtContratAssocie(ReservationWithContratRequestDTO requestDTO);
}