package com.novaerp.service;

import com.novaerp.domain.entity.user.Role;
import com.novaerp.domain.entity.user.User;
import com.novaerp.domain.repository.PasswordResetTokenRepository;
import com.novaerp.domain.repository.UserRepository;
import com.novaerp.security.JwtService;
import com.novaerp.web.dto.requests.LoginRequest;
import com.novaerp.web.dto.requests.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordResetTokenRepository resetTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private RedisTokenService redisTokenService;
    @Mock private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_success_returns_token() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("u1");
        req.setEmail("e1@d.com");
        req.setPassword("pwd");

        when(userRepository.existsByUsername("u1")).thenReturn(false);
        when(userRepository.existsByEmail("e1@d.com")).thenReturn(false);
        when(passwordEncoder.encode("pwd")).thenReturn("encoded");
        when(jwtService.generateToken("u1", "ROLE_USER")).thenReturn("jwt-token");

        var resp = authService.register(req);

        assertNotNull(resp);
        assertEquals("jwt-token", resp.getToken());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void login_success() {
        LoginRequest req = new LoginRequest();
        req.setUsernameOrEmail("u1");
        req.setPassword("pwd");

        User u = new User();
        u.setUsername("u1");
        u.setPassword("encoded");
        u.setRole(Role.valueOf("ROLE_USER"));
        u.setActif(true);

        when(userRepository.findByUsername("u1")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("pwd","encoded")).thenReturn(true);
        when(jwtService.generateToken("u1","ROLE_USER")).thenReturn("token");

        var resp = authService.login(req);

        assertEquals("token", resp.getToken());
    }

    @Test
    void logout_blacklists_token_in_redis() {
        String token = UUID.randomUUID().toString();
        // mock expiration date parse
        java.util.Date future = java.util.Date.from(Instant.now().plusSeconds(3600));
        when(jwtService.getExpiration(token)).thenReturn(future);

        authService.logout(token);

        verify(redisTokenService, times(1)).blacklistToken(eq(token), anyLong());
    }
}
