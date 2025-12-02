package com.novaerp.domain.entity.user;

import jakarta.persistence.*;
import java.time.Instant;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "blacklisted_tokens", indexes = {
    @Index(name = "idx_blacklist_token", columnList = "token")
})
public class BlacklistedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @Column(nullable = false)
    private Instant expiryDate;

    // getters / setters
    
}
