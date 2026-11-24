package tn.esprit.autoloc.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import tn.esprit.autoloc.domain.StatutReservation;

public record ReservationRequest(
        @NotNull(message = "La date de début est obligatoire")
        LocalDate dateDebut,
        @NotNull(message = "La date de fin est obligatoire")
        LocalDate dateFin,
        StatutReservation statut,
        @NotNull(message = "Le véhicule est obligatoire")
        Long vehiculeId,
        @NotNull(message = "Le client est obligatoire")
        Long clientId) {
}