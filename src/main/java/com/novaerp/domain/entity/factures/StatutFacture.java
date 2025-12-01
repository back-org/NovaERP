package com.novaerp.domain.entity.factures;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Statut métier de la facture")
public enum StatutFacture {

    @Schema(description = "Facture en cours de préparation, non envoyée au client")
    BROUILLON,

    @Schema(description = "Facture envoyée au client mais non totalement payée et non en retard")
    ENVOYEE,

    @Schema(description = "Facture totalement réglée")
    PAYEE,

    @Schema(description = "Facture non réglée ou partiellement réglée après la date d'échéance")
    EN_RETARD
}