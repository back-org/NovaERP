package com.novaerp.domain.repository;

import com.novaerp.domain.entity.factures.Facture;
import com.novaerp.domain.entity.factures.StatutFacture;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface FactureRepository extends JpaRepository<Facture, Long> {

    List<Facture> findByClientId(Long clientId);

    List<Facture> findByStatut(StatutFacture statut);

    List<Facture> findByClientIdAndStatut(Long clientId, StatutFacture statut);

    /**
     * CA mensuel (factures payées) sur une période donnée
     */
    @Query(value = """
        SELECT EXTRACT(YEAR FROM f.date_emission) as year,
               EXTRACT(MONTH FROM f.date_emission) as month,
               SUM(f.montant_ttc) as total
        FROM factures f
        WHERE f.statut = 'PAYEE'
          AND f.date_emission BETWEEN :startDate AND :endDate
        GROUP BY year, month
        ORDER BY year, month
        """, nativeQuery = true)
    List<Object[]> findMonthlyRevenueBetween(@Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    /**
     * Nombre de factures en retard (EN_RETARD)
     */
    long countByStatut(StatutFacture statut);

    /**
     * Montant total restant des factures en retard
     */
    @Query("SELECT COALESCE(SUM(f.montantRestant), 0) FROM Facture f WHERE f.statut = 'EN_RETARD'")
    java.math.BigDecimal sumMontantRestantEnRetard();

    /**
     * Top clients par CA (factures payées)
     */
    @Query(value = """
        SELECT c.id as client_id,
               c.nom as client_nom,
               SUM(f.montant_ttc) as total
        FROM factures f
        JOIN clients c ON f.client_id = c.id
        WHERE f.statut = 'PAYEE'
        GROUP BY c.id, c.nom
        ORDER BY total DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findTopClients(@Param("limit") int limit);

}