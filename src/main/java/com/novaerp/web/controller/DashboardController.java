package com.novaerp.web.controller;

import com.novaerp.service.DashboardService;
import com.novaerp.web.dto.responses.DashboardResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@Tag(name = "Dashboard", description = "Statistiques et indicateurs financiers (CA, impayés, top clients)")
@RestController
@RequestMapping("/api/dashboard")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Dashboard global
     * Exemple : GET /api/dashboard?year=2025
     */
	@Operation(
        summary = "Récupérer les données du tableau de bord",
        description = "Retourne le chiffre d'affaires mensuel (liste mois/total), le nombre de factures en retard, le montant total impayé, et le top clients.",
        parameters = {
            @Parameter(name = "year", description = "Année pour laquelle calculer les stats (par défaut : année en cours)", example = "2025")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Données du dashboard retournées",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = DashboardResponse.class))),
            @ApiResponse(responseCode = "401", description = "Non authentifié (JWT requis)")
        }
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<DashboardResponse> getDashboard(
            @Parameter(description = "Année ciblée pour les statistiques", example = "2025")
            @RequestParam(required = false) Integer year)  {

        DashboardResponse response = dashboardService.getDashboard(year);
        return ResponseEntity.ok(response);
    }
}
