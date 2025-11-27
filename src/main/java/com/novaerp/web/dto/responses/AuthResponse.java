package com.novaerp.web.dto.responses;

import lombok.*;

@Builder
@Data
public class AuthResponse {

    private String token;
    private String username;
    private String role;

    public AuthResponse(String token, String username, String role) {
        this.token = token;
        this.username = username;
        this.role = role;
    }

    // getters / setters
}
