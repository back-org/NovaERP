package com.novaerp.web.controller;

import com.novaerp.domain.entity.user.User;
import com.novaerp.domain.repository.UserRepository;
import com.novaerp.security.JwtService;
import com.novaerp.web.dto.requests.LoginRequest;
import com.novaerp.web.dto.requests.RegisterRequest;
import com.novaerp.web.dto.responses.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Tag(name = "Authentification", description = "Endpoints d'authentification et de génération de JWT")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
	
	@Operation(
        summary = "Authentifier un utilisateur",
        description = "Authentifie un utilisateur avec son nom d'utilisateur et mot de passe, "
                    + "et retourne un token JWT à utiliser dans les autres appels protégés."
    )
    @ApiResponse(responseCode = "200", description = "Authentification réussie, token JWT retourné")
    @ApiResponse(responseCode = "401", description = "Identifiants invalides")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                request.getUsername(), request.getPassword()
        );
        authenticationManager.authenticate(auth);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRole().name())
                .build();

        String token = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(
                token,
                user.getUsername(),
                user.getRole().name()
        ));
    }

    // (Optionnel) endpoint register si tu veux créer des users via API
	 @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        AuthResponse resp = authService.register(req);
        return ResponseEntity.ok(resp);
    }

    @Operation(summary = "Login (username/email + password)")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        AuthResponse resp = authService.login(req);
        return ResponseEntity.ok(resp);
    }

    @Operation(summary = "Logout (blacklist current JWT)")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            authService.logout(token);
        }
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Request password reset (returns token for demo)")
    @PostMapping("/password/reset-request")
    public ResponseEntity<String> requestReset(@Valid @RequestBody ResetRequest req) {
        String token = authService.requestPasswordReset(req);
        // In prod, you'd return 200 OK and send email with the token so we don't leak it in response.
        return ResponseEntity.ok(token);
    }

    @Operation(summary = "Confirm password reset (token + newPassword)")
    @PostMapping("/password/reset-confirm")
    public ResponseEntity<Void> confirmReset(@Valid @RequestBody ResetConfirmRequest req) {
        authService.confirmPasswordReset(req);
        return ResponseEntity.noContent().build();
    }
}
