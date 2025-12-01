package com.novaerp.domain.entity.produit;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "produits")
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Enumerated(EnumType.STRING)    
    @Column(name = "type_produit", length = 20, nullable = false)
    private TypeProduit type;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal prixHt;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal tauxTva; // ex: 20.00 pour 20%

    private String unite; // ex: "heure", "pièce"

    // getters / setters
}
