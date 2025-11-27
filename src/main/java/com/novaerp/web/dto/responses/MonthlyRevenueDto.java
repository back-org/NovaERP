package com.novaerp.web.dto.responses;

import lombok.*;
import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyRevenueDto {

    private int year;
    private int month; // 1-12
    private BigDecimal totalTtc;

    // getters / setters
}
