package com.novaerp.service;

import com.novaerp.domain.entity.factures.Facture;
import com.novaerp.web.dto.requests.FactureCreateRequest;
import com.novaerp.web.dto.requests.LigneFactureRequest;
import com.novaerp.web.dto.requests.PaiementCreateRequest;

import java.time.LocalDate;
import java.util.List;

public interface FacturationService {

    Facture creerFacture(Long clientId,
                         List<LigneFactureRequest> lignesRequest,
                         LocalDate dateEcheance);

    Facture getFactureById(Long id);

    List<Facture> rechercherFactures(Long clientId, String statut);

    Facture envoyerFacture(Long id);

    Facture creerFacture(FactureCreateRequest request);

    Facture enregistrerPaiement(Long factureId, PaiementCreateRequest request);
}
