package com.novaerp.domain.repository;

import com.novaerp.domain.entity.factures.Facture;
import com.novaerp.domain.entity.factures.StatutFacture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface FactureRepository extends JpaRepository<Facture, Long> {

    List<Facture> findByClientId(Long clientId);

    List<Facture> findByStatut(StatutFacture statut);

    List<Facture> findByClientIdAndStatut(Long clientId, StatutFacture statut);
}