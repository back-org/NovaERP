package com.novaerp.web.controller;

import com.novaerp.service.DashboardService;
import com.novaerp.web.dto.responses.DashboardResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@Tag(name = "Dashboard", description = "Endpoints de statistiques et d'analytics")
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
        summary = "Récupérer les statistiques globales",
        description = "Retourne le chiffre d'affaires mensuel, le nombre de factures en retard, "
                    + "le montant total impayé et le top 5 des clients."
    )
    @ApiResponse(responseCode = "200", description = "Données du dashboard retournées")
    @GetMapping
    // @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") // si tu veux
    public ResponseEntity<DashboardResponse> getDashboard(
            @Parameter(description = "Année ciblée pour les statistiques", example = "2025")
            @RequestParam(required = false) Integer year)  {

        DashboardResponse response = dashboardService.getDashboard(year);
        return ResponseEntity.ok(response);
    }
}
