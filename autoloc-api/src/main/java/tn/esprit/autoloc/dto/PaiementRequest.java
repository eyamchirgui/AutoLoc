package tn.esprit.autoloc.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import tn.esprit.autoloc.domain.ModePaiement;

public record PaiementRequest(
        @NotNull(message = "Le montant est obligatoire")
        @Positive(message = "Le montant doit être strictement positif")
        BigDecimal montant,
        @NotNull(message = "La date de paiement est obligatoire")
        LocalDate datePaiement,
        @NotNull(message = "Le mode de paiement est obligatoire")
        ModePaiement modePaiement) {
}