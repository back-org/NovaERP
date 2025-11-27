package com.novaerp.web.dto.requests;

import jakarta.validation.constraints.NotBlank;

import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    // getters / setters
}