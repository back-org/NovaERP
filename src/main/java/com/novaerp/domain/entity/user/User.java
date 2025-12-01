package com.novaerp.domain.entity.user;

import jakarta.persistence.*;
import lombok.*;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password; // hashé (BCrypt)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
	@Schema(description = "Rôle de l'utilisateur", example = "ROLE_ADMIN")
    private Role role;

    private Boolean actif = true;

    // getters / setters
}
