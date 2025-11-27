package com.novaerp.web.controller;

import com.novaerp.domain.entity.client.Client;
import com.novaerp.domain.entity.factures.Facture;
import com.novaerp.domain.entity.factures.LigneFacture;
import com.novaerp.service.FacturationService;
import com.novaerp.web.dto.requests.FactureCreateRequest;
import com.novaerp.web.dto.responses.ClientSummaryResponse;
import com.novaerp.web.dto.responses.FactureResponse;
import com.novaerp.web.dto.responses.LigneFactureResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.util.stream.Collectors.toList;

//@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
@RestController
@RequestMapping("/api/factures")
public class FactureController {

    private final FacturationService facturationService;

    public FactureController(FacturationService facturationService) {
        this.facturationService = facturationService;
    }

    /**
     * Création d'une facture avec ses lignes
     */
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    @PostMapping
    public ResponseEntity<FactureResponse> creerFacture(
            @Valid @RequestBody FactureCreateRequest request) {

        Facture facture = facturationService.creerFacture(
                request.getClientId(),
                request.getLignes(),
                request.getDateEcheance()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toFactureResponse(facture));
    }

    /**
     * Récupérer une facture par son id
     */
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    @GetMapping("/{id}")
    public ResponseEntity<FactureResponse> getFacture(@PathVariable Long id) {
        Facture facture = facturationService.getFactureById(id);
        return ResponseEntity.ok(toFactureResponse(facture));
    }

    /**
     * Liste des factures (filtre simple par clientId et/ou statut)
     */
    @GetMapping
    public ResponseEntity<List<FactureResponse>> listFactures(
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) String statut // ex: ENVOYEE, PAYEE...
    ) {
        List<Facture> factures = facturationService.rechercherFactures(clientId, statut);

        List<FactureResponse> responses = factures.stream()
                .map(this::toFactureResponse)
                .collect(toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * Marquer une facture comme envoyée
     */
    @PostMapping("/{id}/envoyer")
    public ResponseEntity<FactureResponse> envoyerFacture(@PathVariable Long id) {
        Facture facture = facturationService.envoyerFacture(id);
        return ResponseEntity.ok(toFactureResponse(facture));
    }

    // ======================
    //   Méthodes de mapping
    // ======================

    private FactureResponse toFactureResponse(Facture facture) {
        FactureResponse dto = new FactureResponse();
        dto.setId(facture.getId());
        dto.setNumero(facture.getNumero());
        dto.setDateEmission(facture.getDateEmission());
        dto.setDateEcheance(facture.getDateEcheance());
        dto.setStatut(facture.getStatut());
        dto.setMontantHt(facture.getMontantHt());
        dto.setMontantTva(facture.getMontantTva());
        dto.setMontantTtc(facture.getMontantTtc());
        dto.setMontantPaye(facture.getMontantPaye());
        dto.setMontantRestant(facture.getMontantRestant());

        dto.setClient(toClientSummary(facture.getClient()));

        List<LigneFactureResponse> lignes = facture.getLignes().stream()
                .map(this::toLigneFactureResponse)
                .collect(toList());
        dto.setLignes(lignes);

        return dto;
    }

    private ClientSummaryResponse toClientSummary(Client client) {
        ClientSummaryResponse dto = new ClientSummaryResponse();
        dto.setId(client.getId());
        dto.setNom(client.getNom());
        dto.setEmail(client.getEmail());
        return dto;
    }

    private LigneFactureResponse toLigneFactureResponse(LigneFacture ligne) {
        LigneFactureResponse dto = new LigneFactureResponse();
        dto.setId(ligne.getId());
        dto.setProduitId(ligne.getProduit().getId());
        dto.setProduitNom(ligne.getProduit().getNom());
        dto.setQuantite(ligne.getQuantite());
        dto.setPrixUnitaireHt(ligne.getPrixUnitaireHt());
        dto.setTauxTva(ligne.getTauxTva());
        dto.setTotalLigneHt(ligne.getTotalLigneHt());
        dto.setTotalLigneTtc(ligne.getTotalLigneTtc());
        return dto;
    }
}
