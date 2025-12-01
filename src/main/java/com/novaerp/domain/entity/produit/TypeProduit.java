package com.novaerp.domain.entity.produit;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Type du produit (bien ou service)")
public enum TypeProduit {

    @Schema(description = "Bien matériel")
    PRODUIT,

    @Schema(description = "Prestation de service")
    SERVICE,

    ABONNEMENT
}
