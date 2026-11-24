package tn.esprit.autoloc.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ContratRequest(
        @NotNull(message = "La date de signature est obligatoire")
        LocalDate dateSignature,
        @NotNull(message = "Le montant total est obligatoire")
        @PositiveOrZero(message = "Le montant total ne peut pas être négatif")
        BigDecimal montantTotal,
        boolean valide,
        @NotNull(message = "La réservation est obligatoire")
        Long reservationId,
        @Valid
        List<PaiementRequest> paiements) {
}