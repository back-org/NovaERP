package com.novaerp.web.dto.responses;

import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientSummaryResponse {

    private Long id;
    private String nom;
    private String email;

    // getters / setters
}
