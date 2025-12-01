package com.novaerp.domain.entity.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Rôle de l'utilisateur dans l'application")
public enum Role {

    @Schema(description = "Administrateur (accès complet)")
    ROLE_ADMIN,

    @Schema(description = "Manager (accès avancé)")
    ROLE_MANAGER,

    @Schema(description = "Utilisateur simple (accès fonctionnel de base)")
    ROLE_USER
}