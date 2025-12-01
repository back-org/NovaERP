package com.novaerp.domain.entity.factures;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "paiements")
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "facture_id")
    private Facture facture;

    @Column(nullable = false)
    private LocalDate datePaiement;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montant;
	
	@Schema(
		description = "Mode de paiement", 
		example = "VIREMENT", 
		allowableValues = {"ESPECES","VIREMENT","CHEQUE","MOBILE_MONEY"}
	)
    @Enumerated(EnumType.STRING)    
    @Column(name = "mode", length = 50, nullable = false)
    private ModePaiement mode;

    private String reference; // ex: référence virement

    // getters / setters
}
