package com.novaerp.service;

import com.novaerp.domain.entity.user.PasswordResetToken;
import com.novaerp.domain.entity.user.Role;
import com.novaerp.domain.entity.user.User;
import com.novaerp.domain.repository.PasswordResetTokenRepository;
import com.novaerp.domain.repository.UserRepository;
import com.novaerp.security.JwtService;


import com.novaerp.web.dto.requests.LoginRequest;
import com.novaerp.web.dto.requests.RegisterRequest;
import com.novaerp.web.dto.requests.ResetConfirmRequest;
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
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RedisTokenService redisTokenService;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository,
                       PasswordResetTokenRepository resetTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       RedisTokenService redisTokenService,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.resetTokenRepository = resetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.redisTokenService = redisTokenService;
        this.emailService = emailService;
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
        u.setRole(Role.valueOf("ROLE_USER"));
        u.setActif(true);
        userRepository.save(u);

        String token = jwtService.generateToken(u.getUsername(), String.valueOf(u.getRole()));
        AuthResponse resp = new AuthResponse();
        resp.setToken(token);
        resp.setUsername(u.getUsername());
        resp.setRole(String.valueOf(u.getRole()));
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
        String token =
                jwtService.generateToken(u.getUsername(),
                String.valueOf(u.getRole()));
        AuthResponse resp = new AuthResponse();
        resp.setToken(token);
        resp.setUsername(u.getUsername());
        resp.setRole(String.valueOf(u.getRole()));
        return resp;
    }

    @Transactional
    public void logout(String token) {
        // compute ttl = expiry - now
        java.util.Date exp = jwtService.getExpiration(token);
        long ttlMillis = exp.toInstant().toEpochMilli() - Instant.now().toEpochMilli();
        if (ttlMillis > 0) {
            redisTokenService.blacklistToken(token, ttlMillis);
        }
    }

    @Transactional
    public void requestPasswordResetAndSendEmail(ResetRequest req) {
        Optional<User> opt = userRepository.findByEmail(req.getEmail());
        // Always return same response to avoid user enumeration
        if (opt.isEmpty()) {
            return;
        }
        User user = opt.get();
        resetTokenRepository.deleteByUser(user);

        PasswordResetToken token = new PasswordResetToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiryDate(Instant.now().plusSeconds(60 * 60)); // 1h
        resetTokenRepository.save(token);

        // Build reset link (adjust host)
        String resetLink = String.format("https://your-domain.com/reset-password?token=%s", token.getToken());

        // Send email
        String subject = "NovaERP - Reset your password";
        String text = "Hello " + user.getUsername() + ",\n\n"
                + "We received a request to reset your password. Click the link below to reset it:\n\n"
                + resetLink + "\n\n"
                + "If you did not request a password reset, ignore this message.\n\n"
                + "Regards,\nNovaERP Team";

        emailService.sendSimpleMessage(user.getEmail(), subject, text);
    }

    @Transactional
    public void confirmPasswordReset(ResetConfirmRequest req) {
        PasswordResetToken tokenEntity = resetTokenRepository.findByToken(req.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid reset token"));
        if (tokenEntity.getExpiryDate().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Reset token expired");
        }
        User u = tokenEntity.getUser();
        u.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(u);
        resetTokenRepository.delete(tokenEntity);
    }
}
