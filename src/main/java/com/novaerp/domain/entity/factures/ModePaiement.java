package com.novaerp.domain.entity.factures;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Mode de paiement utilisé pour une facture")
public enum ModePaiement {

    @Schema(description = "Paiement en espèces")
    ESPECES,

    @Schema(description = "Virement bancaire")
    VIREMENT,

    @Schema(description = "Paiement par chèque")
    CHEQUE,

    @Schema(description = "Paiement par mobile money")
    MOBILE_MONEY
}