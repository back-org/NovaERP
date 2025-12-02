package com.novaerp.security;

import com.novaerp.domain.entity.user.User;
import com.novaerp.domain.repository.UserRepository;
import com.novaerp.service.RedisTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Filtre JWT qui :
 *  - extrait le token Authorization Bearer
 *  - vérifie la validité via JwtService
 *  - vérifie si le token est blacklisté (RedisTokenService)
 *  - charge l'utilisateur depuis la base et crée l'Authentication si tout est OK
 *
 * Note : le filtre nève pas les exceptions en sortie (sauf en cas de token blacklisté -> 401).
 */

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RedisTokenService redisTokenService;

    public JwtAuthFilter(JwtService jwtService,
                         UserRepository userRepository,
                        RedisTokenService redisTokenService) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.redisTokenService = redisTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String token = extractTokenFromRequest(request);
            if (token != null) {

                // 1) check blacklist (stateless logout)
                if (redisTokenService.isBlacklisted(token)) {
                    log.warn("JWT is blacklisted (logout) - rejecting request");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Token is invalid (blacklisted).\"}");
                    return;
                }

                // 2) validate token signature / expiry
                boolean valid = jwtService.validateToken(token);
                if (!valid) {
                    // invalid token -> do not authenticate, but proceed (or return 401 if desired)
                    log.debug("JWT validation failed (invalid or expired) for request: {} {}", request.getMethod(), request.getRequestURI());
                } else {
                    // 3) extract subject (username) and load user
                    String username = jwtService.extractUsername(token);
                    if (StringUtils.hasText(username)) {
                        Optional<User> optUser = userRepository.findByUsername(username);
                        if (optUser.isPresent()) {
                            User user = optUser.get();

                            // build authorities from stored role (assumes single role string like "ROLE_USER")
                            var authorities = List.of(new SimpleGrantedAuthority(String.valueOf(user.getRole())));

                            // create Authentication token and set in context
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(user.getUsername(), null, authorities);
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        } else {
                            log.debug("User not found for username from JWT: {}", username);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            // catch-all: don't break the filter chain for unexpected errors,
            // but log so we can inspect issues in dev / prod.
            log.error("Unexpected error while processing JWT authentication: {}", ex.getMessage(), ex);
        }

        // continue filter chain (request will be unauthenticated if we didn't set authentication)
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the token from the Authorization header (Bearer token).
     * Returns null if header missing or not a Bearer token.
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (!StringUtils.hasText(header)) {
            return null;
        }
        if (header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    /**
     * We override to avoid applying the filter to swagger / public endpoints if desired.
     * Alternatively filtering of paths is done in SecurityConfig using antMatchers.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // Optionally skip filter for public endpoints (swagger, auth, static assets).
        String path = request.getRequestURI();
        return path.startsWith("/api/auth")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/swagger-ui.html")
                || path.startsWith("/actuator");
    }
}
