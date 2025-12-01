package com.novaerp.web.dto.requests;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import lombok.*;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requête de création d'une facture")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FactureCreateRequest {

    @Schema(description = "Identifiant du client", example = "1")
    @NotNull
    private Long clientId;

    @Schema(description = "Date d'échéance de la facture", example = "2025-12-31")
    @NotNull
    @FutureOrPresent
    private LocalDate dateEcheance;

    @Schema(description = "Liste des lignes de facturation")
    @NotEmpty
    private List<LigneFactureRequest> lignes;

    // getters / setters
}
