package com.novaerp.web.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    
	@NotBlank
    private String username;
	
    @Email
    @NotBlank
    private String email;
	
    @NotBlank
    private String password;

    // getters / setters
   
}
