package com.novaerp.web.controller;

import com.novaerp.service.DashboardService;
import com.novaerp.web.dto.responses.DashboardResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Dashboard global
     * Exemple : GET /api/dashboard?year=2025
     */
    @GetMapping
    // @PreAuthorize("hasAnyRole('ADMIN','MANAGER')") // si tu veux
    public ResponseEntity<DashboardResponse> getDashboard(
            @RequestParam(required = false) Integer year) {

        DashboardResponse response = dashboardService.getDashboard(year);
        return ResponseEntity.ok(response);
    }
}
