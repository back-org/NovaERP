package com.novaerp.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NovaERP API")
                        .version("v1")
                        .description(
                                "NovaERP est une API ERP moderne développée avec Spring Boot 3. \n"
                                + "Elle fournit un ensemble complet de fonctionnalités pour la gestion des clients, "
                                + "des produits, des factures, des paiements et du tableau de bord analytique.\n\n"
                                + "L'API implémente une architecture claire et modulaire reposant sur :\n"
                                + "- Spring Boot 3 (REST, validation, configuration)\n"
                                + "- Spring Security 6 + JWT (authentification stateless)\n"
                                + "- Spring Data JPA / Hibernate (ORM)\n"
                                + "- Flyway (migrations SQL)\n"
                                + "- MySQL (base de données principale)\n"
                                + "- Docker & Docker Compose (environnements reproductibles)\n"
                                + "- Swagger / OpenAPI 3 (documentation interactive)\n\n"
                                + "Fonctionnalités principales :\n"
                                + "- Gestion complète des clients et produits\n"
                                + "- Création de factures avec calcul automatique HT / TVA / TTC\n"
                                + "- Suivi des statuts : BROUILLON, ENVOYEE, PAYEE, EN_RETARD\n"
                                + "- Paiements partiels ou complets avec mise à jour du solde\n"
                                + "- Dashboard analytique : CA mensuel, impayés, top clients\n"
                                + "- API sécurisée via JWT\n\n"
                                + "API pensée pour être professionnelle, modulaire et évolutive."
                        )
                )
                .addSecurityItem(new SecurityRequirement()
                        .addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                );
    }
}
