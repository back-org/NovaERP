package com.novaerp.service.impl;

import com.novaerp.domain.entity.client.Client;
import com.novaerp.domain.entity.factures.Facture;
import com.novaerp.domain.entity.factures.Paiement;
import com.novaerp.domain.entity.factures.LigneFacture;
import com.novaerp.domain.entity.factures.StatutFacture;
import com.novaerp.domain.entity.produit.Produit;
import com.novaerp.domain.repository.ClientRepository;
import com.novaerp.domain.repository.FactureRepository;
import com.novaerp.domain.repository.PaiementRepository;
import com.novaerp.domain.repository.ProduitRepository;
import com.novaerp.exceptions.BusinessException;
import com.novaerp.exceptions.ResourceNotFoundException;
import com.novaerp.service.FacturationService;
import com.novaerp.web.dto.requests.FactureCreateRequest;
import com.novaerp.web.dto.requests.LigneFactureRequest;
import com.novaerp.web.dto.requests.PaiementCreateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class FacturationServiceImpl implements FacturationService {

    private final FactureRepository factureRepository;
    private final ClientRepository clientRepository;
    private final ProduitRepository produitRepository;
    private final PaiementRepository paiementRepository;

    public FacturationServiceImpl(FactureRepository factureRepository,
                                  ClientRepository clientRepository,
                                  ProduitRepository produitRepository,
                                  PaiementRepository paiementRepository) {
        this.factureRepository = factureRepository;
        this.clientRepository = clientRepository;
        this.produitRepository = produitRepository;
        this.paiementRepository = paiementRepository;
    }

    // ==========================
    //   Création de facture
    // ==========================

    @Override
    public Facture creerFacture(FactureCreateRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable"));

        if (Boolean.FALSE.equals(client.getActif())) {
            throw new BusinessException("Impossible de créer une facture pour un client inactif");
        }

        Facture facture = new Facture();
        facture.setClient(client);
        facture.setDateEmission(LocalDate.now());
        facture.setDateEcheance(request.getDateEcheance());
        facture.setStatut(StatutFacture.BROUILLON);
        facture.setNumero(genererNumeroFacture());

        List<LigneFacture> lignes = new ArrayList<>();

        for (LigneFactureRequest ligneReq : request.getLignes()) {
            Produit produit = produitRepository.findById(ligneReq.getProduitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable id=" + ligneReq.getProduitId()));

            LigneFacture ligne = new LigneFacture();
            ligne.setFacture(facture);
            ligne.setProduit(produit);
            ligne.setQuantite(ligneReq.getQuantite());
            ligne.setPrixUnitaireHt(produit.getPrixHt());
            ligne.setTauxTva(produit.getTauxTva());

            lignes.add(ligne);
        }

        facture.setLignes(lignes);
        facture.recalculerMontants();

        return factureRepository.save(facture);
    }

    private String genererNumeroFacture() {
        long count = factureRepository.count() + 1;
        String year = String.valueOf(LocalDate.now().getYear());
        return String.format("FAC-%s-%04d", year, count);
    }

    // ==========================
    //   Lecture / recherche
    // ==========================

    @Override
    @Transactional
    public Facture creerFacture(Long clientId, List<LigneFactureRequest> lignesRequest, LocalDate dateEcheance) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable"));

        Facture facture = new Facture();
        facture.setClient(client);
        facture.setDateEmission(LocalDate.now());
        facture.setDateEcheance(dateEcheance);
        facture.setStatut(StatutFacture.BROUILLON);
        facture.setNumero(genererNumeroFacture());

        for (LigneFactureRequest req : lignesRequest) {
            Produit produit = produitRepository.findById(req.getProduitId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable"));

            LigneFacture ligne = new LigneFacture();
            ligne.setFacture(facture);
            ligne.setProduit(produit);
            ligne.setQuantite(req.getQuantite());
            ligne.setPrixUnitaireHt(produit.getPrixHt());
            ligne.setTauxTva(produit.getTauxTva());

            facture.getLignes().add(ligne);
        }

        facture.recalculerMontants();

        return factureRepository.save(facture);
    }

    @Override
    @Transactional(readOnly = true)
    public Facture getFactureById(Long id) {
        return factureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Facture introuvable id=" + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Facture> rechercherFactures(Long clientId, String statut) {
        if (clientId != null && statut != null) {
            StatutFacture statutFacture = parseStatut(statut);
            return factureRepository.findByClientIdAndStatut(clientId, statutFacture);
        } else if (clientId != null) {
            return factureRepository.findByClientId(clientId);
        } else if (statut != null) {
            StatutFacture statutFacture = parseStatut(statut);
            return factureRepository.findByStatut(statutFacture);
        } else {
            return factureRepository.findAll();
        }
    }

    private StatutFacture parseStatut(String statut) {
        try {
            return StatutFacture.valueOf(statut.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Statut de facture invalide : " + statut);
        }
    }

    // ==========================
    //   Envoi de facture
    // ==========================

    @Override
    public Facture envoyerFacture(Long id) {
        Facture facture = getFactureById(id);

        if (facture.getStatut() == StatutFacture.PAYEE) {
            throw new BusinessException("Impossible d'envoyer une facture déjà payée");
        }

        facture.setStatut(StatutFacture.ENVOYEE);
        facture.setDateEmission(LocalDate.now());

        // Màj statut si déjà en retard
        mettreAJourStatutSelonEcheanceEtPaiement(facture);

        return factureRepository.save(facture);
    }

    // ==========================
    //   Paiements
    // ==========================


    public Facture enregistrerPaiement(Long factureId, PaiementCreateRequest request) {
        Facture facture = getFactureById(factureId);

        if (facture.getStatut() == StatutFacture.BROUILLON) {
            throw new BusinessException("Impossible d'enregistrer un paiement sur une facture en brouillon");
        }

        Paiement paiement = new Paiement();
        paiement.setFacture(facture);
        paiement.setDatePaiement(request.getDatePaiement());
        paiement.setMontant(request.getMontant());
        paiement.setMode(request.getMode());
        paiement.setReference(request.getReference());

        // Ajoute le paiement à la facture (met à jour montant payé + restant)
        facture.ajouterPaiement(paiement);

        // met à jour le statut suivant les montants / échéance
        mettreAJourStatutSelonEcheanceEtPaiement(facture);

        paiementRepository.save(paiement);
        return factureRepository.save(facture);
    }

    // ==========================
    //   Règles métier statut
    // ==========================

    private void mettreAJourStatutSelonEcheanceEtPaiement(Facture facture) {
        LocalDate today = LocalDate.now();

        if (facture.getMontantRestant() != null
                && facture.getMontantRestant().compareTo(java.math.BigDecimal.ZERO) == 0) {
            facture.setStatut(StatutFacture.PAYEE);
        } else if (facture.getDateEcheance() != null
                && facture.getDateEcheance().isBefore(today)) {
            facture.setStatut(StatutFacture.EN_RETARD);
        } else if (facture.getStatut() == StatutFacture.BROUILLON) {
            // on ne touche pas
        } else {
            facture.setStatut(StatutFacture.ENVOYEE);
        }
    }
}
