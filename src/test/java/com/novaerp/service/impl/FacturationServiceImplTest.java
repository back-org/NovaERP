package com.novaerp.service.impl;

import com.novaerp.domain.entity.client.Client;
import com.novaerp.domain.entity.factures.Facture;
import com.novaerp.domain.entity.factures.ModePaiement;
import com.novaerp.domain.entity.factures.Paiement;
import com.novaerp.domain.entity.factures.StatutFacture;
import com.novaerp.domain.entity.produit.Produit;
import com.novaerp.domain.repository.ClientRepository;
import com.novaerp.domain.repository.FactureRepository;
import com.novaerp.domain.repository.PaiementRepository;
import com.novaerp.domain.repository.ProduitRepository;
import com.novaerp.exceptions.BusinessException;
import com.novaerp.exceptions.ResourceNotFoundException;
import com.novaerp.web.dto.requests.FactureCreateRequest;
import com.novaerp.web.dto.requests.LigneFactureRequest;
import com.novaerp.web.dto.requests.PaiementCreateRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FacturationServiceImplTest {

    private FactureRepository factureRepository;
    private ClientRepository clientRepository;
    private ProduitRepository produitRepository;
    private PaiementRepository paiementRepository;

    private FacturationServiceImpl facturationService;

    @BeforeEach
    void setUp() {
        factureRepository = mock(FactureRepository.class);
        clientRepository = mock(ClientRepository.class);
        produitRepository = mock(ProduitRepository.class);
        paiementRepository = mock(PaiementRepository.class);

        facturationService = new FacturationServiceImpl(
                factureRepository,
                clientRepository,
                produitRepository,
                paiementRepository
        );
    }

    @Test
    void creerFacture_ok_calcul_montants() {
        // GIVEN
        Long clientId = 1L;
        Long produitId = 10L;

        Client client = new Client();
        client.setId(clientId);
        client.setActif(true);
        client.setNom("Client Test");

        Produit produit = new Produit();
        produit.setId(produitId);
        produit.setNom("Dev backend");
        produit.setPrixHt(new BigDecimal("200000"));
        produit.setTauxTva(new BigDecimal("20"));

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(produitRepository.findById(produitId)).thenReturn(Optional.of(produit));

        FactureCreateRequest request = new FactureCreateRequest();
        request.setClientId(clientId);
        request.setDateEcheance(LocalDate.now().plusDays(7));

        LigneFactureRequest ligneReq = new LigneFactureRequest();
        ligneReq.setProduitId(produitId);
        ligneReq.setQuantite(new BigDecimal("2"));

        request.setLignes(List.of(ligneReq));

        when(factureRepository.count()).thenReturn(0L);
        when(factureRepository.save(any(Facture.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        Facture facture = facturationService.creerFacture(request);

        // THEN
        assertNotNull(facture.getNumero());
        assertEquals(clientId, facture.getClient().getId());
        assertEquals(1, facture.getLignes().size());

        // 2 x 200 000 = 400 000 HT
        assertEquals(new BigDecimal("400000.00"), facture.getMontantHt());
        // TVA 20% -> 80 000
        assertEquals(new BigDecimal("80000.00"), facture.getMontantTva());
        // TTC 480 000
        assertEquals(new BigDecimal("480000.00"), facture.getMontantTtc());
        assertEquals(StatutFacture.BROUILLON, facture.getStatut());
    }

    @Test
    void creerFacture_client_inactif_declenche_BusinessException() {
        Client client = new Client();
        client.setId(1L);
        client.setActif(false);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        FactureCreateRequest request = new FactureCreateRequest();
        request.setClientId(1L);
        request.setDateEcheance(LocalDate.now().plusDays(7));
        request.setLignes(List.of()); // peu importe ici

        assertThrows(BusinessException.class, () -> facturationService.creerFacture(request));
    }

    @Test
    void enregistrerPaiement_ok_met_a_jour_statut_et_montants() {
        // GIVEN
        Facture facture = new Facture();
        facture.setId(100L);
        facture.setDateEcheance(LocalDate.now().minusDays(1)); // passé
        facture.setStatut(StatutFacture.ENVOYEE);
        facture.setMontantHt(new BigDecimal("400000"));
        facture.setMontantTva(new BigDecimal("80000"));
        facture.setMontantTtc(new BigDecimal("480000"));
        facture.setMontantPaye(new BigDecimal("200000"));
        facture.setMontantRestant(new BigDecimal("280000"));

        when(factureRepository.findById(100L)).thenReturn(Optional.of(facture));
        when(paiementRepository.save(any(Paiement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(factureRepository.save(any(Facture.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaiementCreateRequest paiementReq = new PaiementCreateRequest();
        paiementReq.setDatePaiement(LocalDate.now());
        paiementReq.setMontant(new BigDecimal("280000"));
        paiementReq.setMode(ModePaiement.VIREMENT);

        // WHEN
        Facture result = facturationService.enregistrerPaiement(100L, paiementReq);

        // THEN
        assertEquals(new BigDecimal("480000.00"), result.getMontantPaye());
        assertEquals(new BigDecimal("0.00"), result.getMontantRestant());
        assertEquals(StatutFacture.PAYEE, result.getStatut());
    }

    @Test
    void enregistrerPaiement_sur_brouillon_declenche_BusinessException() {
        Facture facture = new Facture();
        facture.setId(200L);
        facture.setStatut(StatutFacture.BROUILLON);

        when(factureRepository.findById(200L)).thenReturn(Optional.of(facture));

        PaiementCreateRequest paiementReq = new PaiementCreateRequest();
        paiementReq.setDatePaiement(LocalDate.now());
        paiementReq.setMontant(new BigDecimal("100000"));
        paiementReq.setMode(ModePaiement.ESPECES);

        assertThrows(BusinessException.class,
                () -> facturationService.enregistrerPaiement(200L, paiementReq));
    }

    @Test
    void getFactureById_not_found_declenche_ResourceNotFound() {
        when(factureRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> facturationService.getFactureById(999L));
    }
}
