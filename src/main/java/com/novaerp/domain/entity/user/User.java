package com.novaerp.domain.entity.user;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_username", columnList = "username"),
    @Index(name = "idx_users_email", columnList = "email")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;
	
	@Column(nullable = false, unique = true, length = 180)
    private String email;

    @Column(nullable = false)
    private String password; // hashé (BCrypt)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
	@Schema(description = "Rôle de l'utilisateur", example = "ROLE_ADMIN")
    private Role role;

    private Boolean actif = true;
	
	@Column(nullable = false)
    private Instant createdAt = Instant.now();

    public boolean isActif() {
        return actif;
    }

    // getters / setters
}
