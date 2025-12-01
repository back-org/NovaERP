package com.novaerp.domain.entity.factures;

import com.novaerp.domain.entity.client.Client;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "factures")
public class Facture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Exemple: FAC-2025-0001
    @Column(nullable = false, unique = true)
    private String numero;

    @ManyToOne(optional = false)
    @JoinColumn(name = "client_id")
    private Client client;

    @Column(nullable = false)
    private LocalDate dateEmission;

    @Column(nullable = false)
    private LocalDate dateEcheance;

	@Schema(
		description = "Statut de la facture",
		example = "ENVOYEE",
		allowableValues = {"BROUILLON","ENVOYEE","PAYEE","EN_RETARD"}
	)
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", length = 30, nullable = false)
    private StatutFacture statut = StatutFacture.BROUILLON;

    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneFacture> lignes = new ArrayList<>();

    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Paiement> paiements = new ArrayList<>();

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montantHt = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montantTva = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montantTtc = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montantPaye = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montantRestant = BigDecimal.ZERO;

    // Méthode métier pour recalculer les montants
    public void recalculerMontants() {
        BigDecimal totalHt = BigDecimal.ZERO;
        BigDecimal totalTva = BigDecimal.ZERO;

        for (LigneFacture ligne : lignes) {
            totalHt = totalHt.add(ligne.getTotalLigneHt());
            // TVA = HT * (taux / 100)
            BigDecimal tvaLigne = ligne.getTotalLigneHt()
                    .multiply(ligne.getTauxTva())
                    .divide(BigDecimal.valueOf(100));
            totalTva = totalTva.add(tvaLigne);
        }

        this.montantHt = totalHt;
        this.montantTva = totalTva;
        this.montantTtc = totalHt.add(totalTva);

        // recalcul du restant
        this.montantRestant = this.montantTtc.subtract(this.montantPaye);
    }

    public void ajouterPaiement(Paiement paiement) {
        this.paiements.add(paiement);
        paiement.setFacture(this);
        this.montantPaye = this.montantPaye.add(paiement.getMontant());
        this.montantRestant = this.montantTtc.subtract(this.montantPaye);
    }

    // getters / setters
}
