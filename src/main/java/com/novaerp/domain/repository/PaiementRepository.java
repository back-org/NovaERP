
package com.novaerp.domain.repository;

import com.novaerp.domain.entity.factures.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaiementRepository extends JpaRepository<Paiement, Long> {
}