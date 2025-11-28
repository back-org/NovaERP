package com.novaerp.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novaerp.domain.entity.client.Client;
import com.novaerp.domain.entity.factures.Facture;
import com.novaerp.domain.entity.factures.StatutFacture;
import com.novaerp.service.FacturationService;
import com.novaerp.web.dto.requests.FactureCreateRequest;
import com.novaerp.web.dto.requests.LigneFactureRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FactureController.class)
class FactureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FacturationService facturationService;

    @Bean
    private ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Test
    void creerFacture_retourne_201_et_factureResponse() throws Exception {
        // GIVEN
        FactureCreateRequest request = new FactureCreateRequest();
        request.setClientId(1L);
        request.setDateEcheance(LocalDate.now().plusDays(7));

        LigneFactureRequest ligne = new LigneFactureRequest();
        ligne.setProduitId(10L);
        ligne.setQuantite(new BigDecimal("2"));
        request.setLignes(List.of(ligne));

        Facture facture = new Facture();
        facture.setId(100L);
        facture.setNumero("FAC-2025-0001");
        facture.setDateEmission(LocalDate.now());
        facture.setDateEcheance(request.getDateEcheance());
        facture.setStatut(StatutFacture.BROUILLON);
        facture.setMontantHt(new BigDecimal("400000"));
        facture.setMontantTva(new BigDecimal("80000"));
        facture.setMontantTtc(new BigDecimal("480000"));
        Client client = new Client();
        client.setId(1L);
        client.setNom("Client Test");
        client.setEmail("client@test.com");
        facture.setClient(client);

        Mockito.when(facturationService.creerFacture(any(FactureCreateRequest.class)))
                .thenReturn(facture);

        // WHEN + THEN
        mockMvc.perform(post("/api/factures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper().writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numero").value("FAC-2025-0001"))
                .andExpect(jsonPath("$.client.id").value(1L))
                .andExpect(jsonPath("$.montantHt").value(400000.0));
    }
}
