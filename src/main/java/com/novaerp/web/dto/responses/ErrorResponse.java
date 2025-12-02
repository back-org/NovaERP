package com.novaerp.web.dto.responses;

import lombok.*;

import java.time.Instant;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
	
    private String error;
	
    private String message;
	
    private String path;
	
    private Integer status;
	
    private Instant timestamp;
	
    // getters/setters
}
