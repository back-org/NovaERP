package com.novaerp.web.controller;

import com.novaerp.service.AuthService;
import com.novaerp.web.dto.requests.LoginRequest;
import com.novaerp.web.dto.requests.RegisterRequest;
import com.novaerp.web.dto.requests.ResetConfirmRequest;
import com.novaerp.web.dto.requests.ResetRequest;
import com.novaerp.web.dto.responses.AuthResponse;

import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentification",
     description = "Gestion des comptes utilisateurs : inscription, connexion (JWT), déconnexion, réinitialisation de mot de passe.")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    @Operation(
        summary = "Inscrire un nouvel utilisateur",
        description = "Crée un nouvel utilisateur. Le mot de passe est hashé (BCrypt). " +
                      "En environnement démo, la réponse contient un JWT ; en production on préférera renvoyer 201 et demander la vérification email.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RegisterRequest.class),
                examples = @ExampleObject(
                    name = "RegisterExample",
                    value = "{\"username\":\"alice\",\"email\":\"alice@example.com\",\"password\":\"P@ssw0rd\"}"
                )
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Inscription réussie, retourne token JWT (demo)",
                content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides ou username/email déjà utilisé"),
            @ApiResponse(responseCode = "500", description = "Erreur serveur")
        }
    )
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        AuthResponse resp = authService.register(req);
        return ResponseEntity.ok(resp);
    }

    @Operation(
        summary = "Connexion (login) - obtenir un JWT",
        description = "S'authentifie via `usernameOrEmail` + `password`. Retourne un JWT signé et les informations minimales de l'utilisateur.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginRequest.class),
                examples = @ExampleObject(value = "{\"usernameOrEmail\":\"alice\",\"password\":\"P@ssw0rd\"}")
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Authentification réussie, retourne token JWT",
                content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Identifiants invalides"),
            @ApiResponse(responseCode = "423", description = "Compte désactivé / bloqué")
        }
    )
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        AuthResponse resp = authService.login(req);
        return ResponseEntity.ok(resp);
    }
	
	@Operation(
        summary = "Logout (invalidate current JWT)",
        description = "Met en blacklist le token courant (stateless logout via Redis). Le token fourni dans Authorization sera rendu invalide.",
        responses = {
            @ApiResponse(responseCode = "204", description = "Déconnexion réussie"),
            @ApiResponse(responseCode = "401", description = "Token manquant ou invalide")
        }
    )    
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            authService.logout(token);
        }
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Demander une réinitialisation de mot de passe",
        description = "Crée un token de reset (valide 1h) et envoie un email contenant le lien de réinitialisation. " +
                      "Pour éviter l'énumération d'emails, la réponse est neutre (toujours 200).",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResetRequest.class),
                examples = @ExampleObject(value = "{\"email\":\"alice@example.com\"}")
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Si l'email existe, un message de reset a été envoyé (ne révèle pas l'existence du compte)")
        }
    )
    @PostMapping("/password/reset-request")
    public ResponseEntity<Void> requestReset(@Valid @RequestBody ResetRequest req) {
        authService.requestPasswordResetAndSendEmail(req);
        return ResponseEntity.ok().build(); // don't return token in response
    }

    @Operation(
        summary = "Confirmer la réinitialisation du mot de passe",
        description = "Valide le token reçu par email et met à jour le mot de passe de l'utilisateur. Le token devient inutilisable après usage.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResetConfirmRequest.class),
                examples = @ExampleObject(value = "{\"token\":\"<TOKEN>\",\"newPassword\":\"N3wP@ss!\"}")
            )
        ),
        responses = {
            @ApiResponse(responseCode = "204", description = "Mot de passe réinitialisé"),
            @ApiResponse(responseCode = "400", description = "Token invalide ou expiré"),
            @ApiResponse(responseCode = "404", description = "Token non trouvé")
        }
    )
    @PostMapping("/password/reset-confirm")
    public ResponseEntity<Void> confirmReset(@Valid @RequestBody ResetConfirmRequest req) {
        authService.confirmPasswordReset(req);
        return ResponseEntity.noContent().build();
    }
}
