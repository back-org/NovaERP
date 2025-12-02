package com.novaerp.web.dto.responses;

import lombok.*;

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
