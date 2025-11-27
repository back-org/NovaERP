package com.novaerp.domain.entity.factures;

import jakarta.persistence.*;
import lombok.*;
import com.novaerp.domain.entity.produit.Produit;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lignes_facture")
public class LigneFacture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "facture_id")
    private Facture facture;

    @ManyToOne(optional = false)
    @JoinColumn(name = "produit_id")
    private Produit produit;

    @Column(nullable = false)
    private BigDecimal quantite;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal prixUnitaireHt;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal tauxTva;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalLigneHt;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalLigneTtc;

    @PrePersist
    @PreUpdate
    public void calculerTotaux() {
        if (quantite == null || prixUnitaireHt == null || tauxTva == null) {
            return;
        }

        this.totalLigneHt = prixUnitaireHt.multiply(quantite);

        BigDecimal tva = totalLigneHt
                .multiply(tauxTva)
                .divide(BigDecimal.valueOf(100));

        this.totalLigneTtc = totalLigneHt.add(tva);
    }

    // getters / setters
}
