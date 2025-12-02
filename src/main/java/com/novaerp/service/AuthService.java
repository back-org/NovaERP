package com.novaerp.service;

import com.novaerp.domain.entity.user.BlacklistedToken;
import com.novaerp.domain.entity.user.PasswordResetToken;
import com.novaerp.domain.entity.user.User;
import com.novaerp.domain.repository.BlacklistedTokenRepository;
import com.novaerp.domain.repository.PasswordResetTokenRepository;
import com.novaerp.domain.repository.UserRepository;
import com.novaerp.security.JwtService;

import com.novaerp.web.dto.requests.LoginRequest;
import com.novaerp.web.dto.requests.RegisterRequest;
import com.novaerp.web.dto.requests.ResetRequest;
import com.novaerp.web.dto.responses.AuthResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordResetTokenRepository resetTokenRepository,
                       BlacklistedTokenRepository blacklistedTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.resetTokenRepository = resetTokenRepository;
        this.blacklistedTokenRepository = blacklistedTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("Username already in use");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }
        User u = new User();
        u.setUsername(req.getUsername());
        u.setEmail(req.getEmail());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setRole("ROLE_USER");
        u.setActif(true);
        userRepository.save(u);

        String token = jwtService.generateToken(u.getUsername(), u.getRole());
        AuthResponse resp = new AuthResponse();
        resp.setToken(token);
        resp.setUsername(u.getUsername());
        resp.setRole(u.getRole());
        return resp;
    }

    public AuthResponse login(LoginRequest req) {
        Optional<User> opt = userRepository.findByUsername(req.getUsernameOrEmail());
        if (opt.isEmpty()) {
            opt = userRepository.findByEmail(req.getUsernameOrEmail());
        }
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        User u = opt.get();
        if (!passwordEncoder.matches(req.getPassword(), u.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        if (!u.isActif()) {
            throw new IllegalStateException("User is inactive");
        }
        String token = jwtService.generateToken(u.getUsername(), u.getRole());
        AuthResponse resp = new AuthResponse();
        resp.setToken(token);
        resp.setUsername(u.getUsername());
        resp.setRole(u.getRole());
        return resp;
    }

    @Transactional
    public void logout(String token) {
        // parse expiration and store token in blacklist until expiry
        java.util.Date expiry = jwtService.getExpiration(token);
        BlacklistedToken bt = new BlacklistedToken();
        bt.setToken(token);
        bt.setExpiryDate(expiry.toInstant());
        blacklistedTokenRepository.save(bt);
    }

    @Transactional
    public String requestPasswordReset(ResetRequest req) {
        Optional<User> opt = userRepository.findByEmail(req.getEmail());
        if (opt.isEmpty()) {
            // don't reveal email existence -> return generic message
            return "If the email exists, a reset link has been generated";
        }
        User user = opt.get();
        // Remove previous tokens
        resetTokenRepository.deleteByUser(user);

        PasswordResetToken token = new PasswordResetToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiryDate(Instant.now().plusSeconds(60 * 60)); // 1h expiry
        resetTokenRepository.save(token);

        // In prod : send email with token link. Here return token for demo.
        return token.getToken();
    }

    @Transactional
    public void confirmPasswordReset(com.novaerp.web.dto.auth.ResetConfirmRequest req) {
        PasswordResetToken tokenEntity = resetTokenRepository.findByToken(req.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid reset token"));
        if (tokenEntity.getExpiryDate().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Reset token expired");
        }
        User u = tokenEntity.getUser();
        u.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(u);
        // cleanup token
        resetTokenRepository.delete(tokenEntity);
        // Optionally blacklist all existing JWTs? You might want to invalidate old tokens:
        // (not implemented here)
    }
}
