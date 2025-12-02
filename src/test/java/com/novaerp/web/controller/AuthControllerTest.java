package com.novaerp.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novaerp.service.AuthService;

import com.novaerp.web.dto.requests.LoginRequest;
import com.novaerp.web.dto.requests.RegisterRequest;
import com.novaerp.web.dto.responses.AuthResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;

    @Bean
    private ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @MockitoBean
    private AuthService authService;

    @Test
    void register_endpoint_returns_token() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("u1");
        req.setEmail("e1@d.com");
        req.setPassword("pwd");

        AuthResponse resp = new AuthResponse();
        resp.setToken("token123");
        resp.setUsername("u1");
        resp.setRole("ROLE_USER");

        Mockito.when(authService.register(any(RegisterRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper().writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("token123")));
    }

    @Test
    void login_endpoint_returns_token() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsernameOrEmail("u1");
        req.setPassword("pwd");

        AuthResponse resp = new AuthResponse();
        resp.setToken("token123");

        Mockito.when(authService.login(any(LoginRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper().writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("token123")));
    }
}
