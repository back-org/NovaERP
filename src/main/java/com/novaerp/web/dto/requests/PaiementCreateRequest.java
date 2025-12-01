package com.novaerp.web.dto.requests;

import com.novaerp.domain.entity.factures.ModePaiement;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requête de création d'un paiement sur une facture")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaiementCreateRequest {

    @Schema(description = "Date du paiement", example = "2025-11-27")
    @NotNull
    private LocalDate datePaiement;

    @Schema(description = "Montant payé", example = "500000")
    @NotNull
    @Min(1)
    private BigDecimal montant;

    @Schema(description = "Mode de paiement", example = "VIREMENT")
    @NotNull
    private ModePaiement mode;

    @Schema(description = "Référence du paiement (bank ref, mobile money, etc.)", example = "VIR-2025-001")
    private String reference;

    // getters / setters
}
