package com.novaerp.web.dto.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetConfirmRequest {
    @NotBlank
    private String token;
    @NotBlank
    private String newPassword;

    // getters/setters   
}
