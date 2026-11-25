package tn.esprit.autoloc.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import tn.esprit.autoloc.domain.StatutReservation;

public record ReservationWithContratRequestDTO(
        @NotNull(message = "La date de début est obligatoire")
        LocalDate dateDebut,
        @NotNull(message = "La date de fin est obligatoire")
        LocalDate dateFin,
        StatutReservation statut,
        @NotNull(message = "Le véhicule est obligatoire")
        Long vehiculeId,
        @NotNull(message = "Le client est obligatoire")
        Long clientId,
        @NotNull(message = "La date de signature du contrat est obligatoire")
        LocalDate dateSignature,
        @PositiveOrZero(message = "Le montant total ne peut pas être négatif")
        BigDecimal montantTotal,
        Boolean valide,
        @Valid
        List<PaiementRequest> paiements) {
}
