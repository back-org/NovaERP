package com.novaerp.web.dto.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.*;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ligne d'une facture (produit + quantité)")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LigneFactureRequest {

    @Schema(description = "Identifiant du produit", example = "10")
    @NotNull
    private Long produitId;

    @Schema(description = "Quantité facturée", example = "2")
    @NotNull
    @Min(1)
    private BigDecimal quantite;

    // getters / setters
}
