package com.novaerp.web.dto.responses;

import java.math.BigDecimal;
import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopClientDto {

    private Long clientId;
    private String clientNom;
    private BigDecimal chiffreAffaires;

    // getters / setters
}