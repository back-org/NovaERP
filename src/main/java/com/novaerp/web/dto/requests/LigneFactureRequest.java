package com.novaerp.web.dto.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LigneFactureRequest {

    @NotNull
    private Long produitId;

    @NotNull
    @Min(1)
    private BigDecimal quantite;

   /* public Long getProduitId() {
        return produitId;
    } */

    // getters / setters
}
