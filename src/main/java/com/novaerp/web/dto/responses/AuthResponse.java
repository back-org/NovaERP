package com.novaerp.web.dto.responses;

import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
	
    private String username;
	
    private String role;

   
    // getters / setters
}
