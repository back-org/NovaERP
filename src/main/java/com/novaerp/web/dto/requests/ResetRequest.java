package com.novaerp.web.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetRequest {
    @Email
    @NotBlank
    private String email;

    // getter / setter    
}