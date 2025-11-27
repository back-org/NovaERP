
package com.novaerp.domain.repository;

import com.novaerp.domain.entity.produit.Produit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProduitRepository extends JpaRepository<Produit, Long> {
}