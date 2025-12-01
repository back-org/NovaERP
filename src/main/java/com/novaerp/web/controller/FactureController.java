package com.novaerp.web.controller;

import com.novaerp.domain.entity.client.Client;
import com.novaerp.domain.entity.factures.Facture;
import com.novaerp.domain.entity.factures.LigneFacture;
import com.novaerp.service.FacturationService;
import com.novaerp.web.dto.requests.FactureCreateRequest;
import com.novaerp.web.dto.requests.PaiementCreateRequest;
import com.novaerp.web.dto.responses.ClientSummaryResponse;
import com.novaerp.web.dto.responses.FactureResponse;
import com.novaerp.web.dto.responses.LigneFactureResponse;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import static java.util.stream.Collectors.toList;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

//@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
@Tag(name = "Factures", description = "Endpoints de gestion des factures")
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
    @Operation(
            summary = "Créer une facture",
            description = "Créer une nouvelle facture pour un client donné avec ses lignes."
    )
    @ApiResponse(responseCode = "201", description = "Facture créée avec succès")
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

    // =========================================================
    //  DÉTAIL D'UNE FACTURE
    // =========================================================
    @Operation(
        summary = "Récupérer une facture par ID",
        description = "Retourne le détail complet d'une facture : client, lignes, montants, statut, paiements."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Facture trouvée",
        content = @Content(schema = @Schema(implementation = FactureResponse.class))
    )
    @ApiResponse(
        responseCode = "404",
        description = "Facture introuvable"
    )
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
    @GetMapping("/{id}")
    public ResponseEntity<FactureResponse> getFacture(@Parameter(description = "Identifiant de la facture", example = "123") @PathVariable Long id) {
        Facture facture = facturationService.getFactureById(id);
        return ResponseEntity.ok(toFactureResponse(facture));
    }

    // =========================================================
    //  LISTER LES FACTURES
    // =========================================================
    @Operation(
        summary = "Lister les factures",
        description = "Retourne la liste des factures enregistrées."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Liste des factures retournée",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = FactureResponse.class)))
    )    
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

   // =========================================================
    //  ENVOYER UNE FACTURE (changer le statut)
    // =========================================================
    @Operation(
        summary = "Envoyer une facture",
        description = "Marque une facture comme envoyée (statut ENVOYEE). "
                    + "Cette opération est utilisée lorsqu'une facture est transmise au client."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Facture envoyée",
        content = @Content(schema = @Schema(implementation = FactureResponse.class))
    )
    @ApiResponse(
        responseCode = "404",
        description = "Facture introuvable"
    )
    @PostMapping("/{id}/envoyer")
    public ResponseEntity<FactureResponse> envoyerFacture(@PathVariable Long id) {
        Facture facture = facturationService.envoyerFacture(id);
        return ResponseEntity.ok(toFactureResponse(facture));
    }

    // =========================================================
    //   ENREGISTRER UN PAIEMENT SUR UNE FACTURE
    // =========================================================
    @Operation(
            summary = "Enregistrer un paiement sur une facture",
            description = "Ajoute un paiement (partiel ou complet) sur une facture existante, "
                    + "met à jour les montants (payé / restant) et le statut (PAYEE / EN_RETARD / ENVOYEE...)."
    )
    @ApiResponse(responseCode = "200", description = "Paiement enregistré, facture mise à jour")
    @ApiResponse(responseCode = "400", description = "Requête invalide ou paiement impossible")
    @ApiResponse(responseCode = "404", description = "Facture introuvable")
    @PostMapping("/{id}/paiements")
    public ResponseEntity<FactureResponse> enregistrerPaiement(
            @Parameter(description = "Identifiant de la facture à régler", example = "123")
            @PathVariable Long id,
            @Valid @RequestBody PaiementCreateRequest request) {

        // Appel du service métier : enregistre le paiement + met à jour la facture
        var facture = facturationService.enregistrerPaiement(id, request);

        // Mapping entité -> DTO de réponse pour l'API
        FactureResponse response = toFactureResponse(facture);

        return ResponseEntity.ok(response);
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
