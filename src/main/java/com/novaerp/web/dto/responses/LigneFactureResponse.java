package com.novaerp.web.dto.responses;

import java.math.BigDecimal;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneFactureResponse {

    private Long id;
    private Long produitId;
    private String produitNom;
    private BigDecimal quantite;
    private BigDecimal prixUnitaireHt;
    private BigDecimal tauxTva;
    private BigDecimal totalLigneHt;
    private BigDecimal totalLigneTtc;

    // getters / setters
}
