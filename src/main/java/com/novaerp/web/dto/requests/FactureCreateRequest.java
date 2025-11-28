package com.novaerp.web.dto.requests;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FactureCreateRequest {

    @NotNull
    private Long clientId;

    @NotNull
    @FutureOrPresent
    private LocalDate dateEcheance;

    @NotEmpty
    private List<LigneFactureRequest> lignes;

    // getters / setters
}
