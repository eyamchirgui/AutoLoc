package tn.esprit.autoloc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AgenceRequest(
        @NotBlank(message = "Le nom de l'agence est obligatoire")
        @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
        String nom,
        @NotBlank(message = "La ville est obligatoire")
        @Size(max = 50, message = "La ville ne doit pas dépasser 50 caractères")
        String ville,
        @NotBlank(message = "L'adresse est obligatoire")
        @Size(max = 150, message = "L'adresse ne doit pas dépasser 150 caractères")
        String adresse,
        @Size(max = 20, message = "Le téléphone ne doit pas dépasser 20 caractères")
        String telephone) {
}