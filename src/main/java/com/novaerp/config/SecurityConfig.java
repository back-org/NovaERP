package com.novaerp.config;

import com.novaerp.domain.repository.BlacklistedTokenRepository;
import com.novaerp.domain.repository.UserRepository;
import com.novaerp.security.JwtAuthFilter;
import com.novaerp.security.JwtService;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
	private final JwtService jwtService;
    private final UserRepository userRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, JwtService jwtService, UserRepository userRepository, BlacklistedTokenRepository blacklistedTokenRepository) {
        this.jwtAuthFilter = jwtAuthFilter;
		this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.blacklistedTokenRepository = blacklistedTokenRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
          .csrf(AbstractHttpConfigurer::disable)
          .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .authorizeHttpRequests(auth -> auth
                  .requestMatchers("/api/auth/**").permitAll()
                  .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll() // Permit access to Swagger UI resources
                  .requestMatchers("/actuator/health").permitAll()
                  //.anyRequest().authenticated()				 
          );
         // .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
