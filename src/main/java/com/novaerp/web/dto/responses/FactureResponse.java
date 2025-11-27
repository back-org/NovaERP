package com.novaerp.web.dto.responses;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.novaerp.domain.entity.factures.StatutFacture;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactureResponse {

    private Long id;
    private String numero;
    private ClientSummaryResponse client;
    private LocalDate dateEmission;
    private LocalDate dateEcheance;
    private StatutFacture statut;

    private BigDecimal montantHt;
    private BigDecimal montantTva;
    private BigDecimal montantTtc;
    private BigDecimal montantPaye;
    private BigDecimal montantRestant;

    private List<LigneFactureResponse> lignes;

    // getters / setters
}
