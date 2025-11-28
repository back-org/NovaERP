package com.novaerp.service.impl;

import com.novaerp.domain.entity.factures.StatutFacture;
import com.novaerp.domain.repository.FactureRepository;
import com.novaerp.web.dto.responses.DashboardResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DashboardServiceImplTest {

    private FactureRepository factureRepository;
    private DashboardServiceImpl dashboardService;

    @BeforeEach
    void setUp() {
        factureRepository = mock(FactureRepository.class);
        dashboardService = new DashboardServiceImpl(factureRepository);
    }

    @Test
    void getDashboard_retourne_CA_mensuel_et_top_clients() {
        int year = 2025;
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);

        // CA mensuel : janvier + février
        List<Object[]> monthlyRows = List.of(
                new Object[]{2025, 1, new BigDecimal("1000000.00")},
                new Object[]{2025, 2, new BigDecimal("1500000.00")}
        );

        when(factureRepository.findMonthlyRevenueBetween(start, end))
                .thenReturn(monthlyRows);

        when(factureRepository.countByStatut(StatutFacture.EN_RETARD))
                .thenReturn(3L);

        when(factureRepository.sumMontantRestantEnRetard())
                .thenReturn(new BigDecimal("750000.00"));

        List<Object[]> topClientsRows = List.of(
                new Object[]{1L, "Société Alpha", new BigDecimal("2000000.00")},
                new Object[]{2L, "Cabinet Beta", new BigDecimal("1500000.00")}
        );

        when(factureRepository.findTopClients(5))
                .thenReturn(topClientsRows);

        // WHEN
        DashboardResponse response = dashboardService.getDashboard(year);

        // THEN
        assertEquals(2, response.getRevenusMensuels().size());
        assertEquals(3, response.getNombreFacturesEnRetard());
        assertEquals(new BigDecimal("750000.00"), response.getMontantTotalEnRetard());
        assertEquals(2, response.getTopClients().size());
        assertEquals("Société Alpha", response.getTopClients().get(0).getClientNom());
    }
}
