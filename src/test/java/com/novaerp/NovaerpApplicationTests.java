package com.novaerp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertNotNull;

@SpringBootTest
class NovaerpApplicationTests {

	@Autowired
    private ApplicationContext applicationContext;

    /**
     * Test de base : vérifie que le contexte Spring Boot démarre sans erreur.
     */
    @Test
    void contextLoads() {
        assertNotNull(String.valueOf(applicationContext), "Le contexte Spring aurait dû être initialisé");
    }

    /**
     * Vérifie que le nom de l'application (spring.application.name)
     * est bien chargé depuis application.yml.
     */
    @Test
    void applicationNameIsSet() {
        String appName = applicationContext.getEnvironment()
                                           .getProperty("spring.application.name");
        assertNotNull(appName, "spring.application.name ne doit pas être null");
        assertEquals("novaerp", appName);
    }

}
