package com.novaerp.web.dto.responses;

import java.math.BigDecimal;
import java.util.List;
import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private List<MonthlyRevenueDto> revenusMensuels;
    private long nombreFacturesEnRetard;
    private BigDecimal montantTotalEnRetard;
    private List<TopClientDto> topClients;

    // getters / setters
}
