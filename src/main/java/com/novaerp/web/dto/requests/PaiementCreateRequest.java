package com.novaerp.web.dto.requests;

import com.novaerp.domain.entity.factures.ModePaiement;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaiementCreateRequest {

    @NotNull
    private LocalDate datePaiement;

    @NotNull
    @Min(1)
    private BigDecimal montant;

    @NotNull
    private ModePaiement mode;

    private String reference;

    /*
    public LocalDate getDatePaiement() {
        return datePaiement;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public ModePaiement getMode() {
        return mode;
    }

    public String getReference() {
        return reference;
    }
    */

    // getters / setters
}
