package com.novaerp.service.impl;

import com.novaerp.domain.entity.factures.StatutFacture;
import com.novaerp.domain.repository.FactureRepository;
import com.novaerp.service.DashboardService;
import com.novaerp.web.dto.responses.DashboardResponse;
import com.novaerp.web.dto.responses.MonthlyRevenueDto;
import com.novaerp.web.dto.responses.TopClientDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final FactureRepository factureRepository;

    public DashboardServiceImpl(FactureRepository factureRepository) {
        this.factureRepository = factureRepository;
    }

    @Override
    public DashboardResponse getDashboard(Integer year) {
        int targetYear = (year != null) ? year : Year.now().getValue();

        LocalDate start = LocalDate.of(targetYear, 1, 1);
        LocalDate end = LocalDate.of(targetYear, 12, 31);

        DashboardResponse response = new DashboardResponse();

        // 1) CA mensuel
        List<Object[]> monthlyRows = factureRepository.findMonthlyRevenueBetween(start, end);
        List<MonthlyRevenueDto> revenusMensuels = new ArrayList<>();

        for (Object[] row : monthlyRows) {
            int y = ((Number) row[0]).intValue();
            int m = ((Number) row[1]).intValue();
            BigDecimal total = (BigDecimal) row[2];

            MonthlyRevenueDto dto = new MonthlyRevenueDto();
            dto.setYear(y);
            dto.setMonth(m);
            dto.setTotalTtc(total);
            revenusMensuels.add(dto);
        }

        response.setRevenusMensuels(revenusMensuels);

        // 2) Nombre de factures en retard
        long nbEnRetard = factureRepository.countByStatut(StatutFacture.EN_RETARD);
        response.setNombreFacturesEnRetard(nbEnRetard);

        // 3) Montant total en retard
        BigDecimal totalRetard = factureRepository.sumMontantRestantEnRetard();
        response.setMontantTotalEnRetard(totalRetard != null ? totalRetard : BigDecimal.ZERO);

        // 4) Top clients
        List<Object[]> topRows = factureRepository.findTopClients(5);
        List<TopClientDto> topClients = new ArrayList<>();

        for (Object[] row : topRows) {
            TopClientDto dto = new TopClientDto();
            dto.setClientId(((Number) row[0]).longValue());
            dto.setClientNom((String) row[1]);
            dto.setChiffreAffaires((BigDecimal) row[2]);
            topClients.add(dto);
        }

        response.setTopClients(topClients);

        return response;
    }
}
