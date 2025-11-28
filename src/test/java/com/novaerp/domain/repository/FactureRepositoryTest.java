package com.novaerp.domain.repository;

import com.novaerp.domain.entity.client.Client;
import com.novaerp.domain.entity.factures.Facture;
import com.novaerp.domain.entity.factures.StatutFacture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests JPA sur FactureRepository avec une base H2 en mémoire.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class FactureRepositoryTest {

    @Autowired
    private FactureRepository factureRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Test
    void findMonthlyRevenueBetween_retourne_CA_mensuel_agrege() {
        // GIVEN
        Client client = new Client();
        client.setNom("Client Dashboard");
        client.setActif(true);
        client = clientRepository.save(client);

        // Facture janvier : 1 000 000 TTC
        Facture fJan = new Facture();
        fJan.setNumero("FAC-2025-0101");
        fJan.setClient(client);
        fJan.setDateEmission(LocalDate.of(2025, 1, 10));
        fJan.setDateEcheance(LocalDate.of(2025, 1, 20));
        fJan.setStatut(StatutFacture.PAYEE);
        fJan.setMontantHt(new BigDecimal("800000"));
        fJan.setMontantTva(new BigDecimal("200000"));
        fJan.setMontantTtc(new BigDecimal("1000000"));
        fJan.setMontantPaye(new BigDecimal("1000000"));
        fJan.setMontantRestant(BigDecimal.ZERO);

        // Facture février : 500 000 TTC
        Facture fFeb = new Facture();
        fFeb.setNumero("FAC-2025-0201");
        fFeb.setClient(client);
        fFeb.setDateEmission(LocalDate.of(2025, 2, 5));
        fFeb.setDateEcheance(LocalDate.of(2025, 2, 15));
        fFeb.setStatut(StatutFacture.PAYEE);
        fFeb.setMontantHt(new BigDecimal("400000"));
        fFeb.setMontantTva(new BigDecimal("100000"));
        fFeb.setMontantTtc(new BigDecimal("500000"));
        fFeb.setMontantPaye(new BigDecimal("500000"));
        fFeb.setMontantRestant(BigDecimal.ZERO);

        factureRepository.save(fJan);
        factureRepository.save(fFeb);

        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 12, 31);

        // WHEN
        List<Object[]> rows = factureRepository.findMonthlyRevenueBetween(start, end);

        // THEN
        assertFalse(rows.isEmpty());
        // On s'attend à 2 lignes : janvier et février
        assertEquals(2, rows.size());

        Object[] janRow = rows.get(0);
        int yearJan = ((Number) janRow[0]).intValue();
        int monthJan = ((Number) janRow[1]).intValue();
        BigDecimal totalJan = (BigDecimal) janRow[2];

        assertEquals(2025, yearJan);
        assertEquals(1, monthJan);
        assertEquals(new BigDecimal("1000000.00"), totalJan);

        Object[] febRow = rows.get(1);
        int yearFeb = ((Number) febRow[0]).intValue();
        int monthFeb = ((Number) febRow[1]).intValue();
        BigDecimal totalFeb = (BigDecimal) febRow[2];

        assertEquals(2025, yearFeb);
        assertEquals(2, monthFeb);
        assertEquals(new BigDecimal("500000.00"), totalFeb);
    }

    @Test
    void countByStatut_et_sumMontantRestantEnRetard_ok() {
        // GIVEN
        Client client = new Client();
        client.setNom("Client Retard");
        client.setActif(true);
        client = clientRepository.save(client);

        // Facture en retard : 300 000 restant
        Facture fLate1 = new Facture();
        fLate1.setNumero("FAC-RET-001");
        fLate1.setClient(client);
        fLate1.setDateEmission(LocalDate.of(2025, 1, 1));
        fLate1.setDateEcheance(LocalDate.of(2025, 1, 10));
        fLate1.setStatut(StatutFacture.EN_RETARD);
        fLate1.setMontantTtc(new BigDecimal("500000"));
        fLate1.setMontantPaye(new BigDecimal("200000"));
        fLate1.setMontantRestant(new BigDecimal("300000"));

        // Deuxième facture en retard : 200 000 restant
        Facture fLate2 = new Facture();
        fLate2.setNumero("FAC-RET-002");
        fLate2.setClient(client);
        fLate2.setDateEmission(LocalDate.of(2025, 2, 1));
        fLate2.setDateEcheance(LocalDate.of(2025, 2, 10));
        fLate2.setStatut(StatutFacture.EN_RETARD);
        fLate2.setMontantTtc(new BigDecimal("200000"));
        fLate2.setMontantPaye(BigDecimal.ZERO);
        fLate2.setMontantRestant(new BigDecimal("200000"));

        factureRepository.save(fLate1);
        factureRepository.save(fLate2);

        // Facture payée (pour vérifier qu'elle n'est pas comptée)
        Facture fPaid = new Facture();
        fPaid.setNumero("FAC-PAID-001");
        fPaid.setClient(client);
        fPaid.setDateEmission(LocalDate.of(2025, 3, 1));
        fPaid.setDateEcheance(LocalDate.of(2025, 3, 10));
        fPaid.setStatut(StatutFacture.PAYEE);
        fPaid.setMontantTtc(new BigDecimal("100000"));
        fPaid.setMontantPaye(new BigDecimal("100000"));
        fPaid.setMontantRestant(BigDecimal.ZERO);

        factureRepository.save(fPaid);

        // WHEN
        long nbEnRetard = factureRepository.countByStatut(StatutFacture.EN_RETARD);
        BigDecimal totalEnRetard = factureRepository.sumMontantRestantEnRetard();

        // THEN
        assertEquals(2L, nbEnRetard);
        assertEquals(new BigDecimal("500000.00"), totalEnRetard);
    }

    @Test
    void findTopClients_retourne_clients_ordonnes_par_CA() {
        // GIVEN
        Client alpha = new Client();
        alpha.setNom("Alpha");
        alpha.setActif(true);
        alpha = clientRepository.save(alpha);

        Client beta = new Client();
        beta.setNom("Beta");
        beta.setActif(true);
        beta = clientRepository.save(beta);

        // Alpha : 2 factures payées = 1 500 000 TTC
        Facture f1 = new Facture();
        f1.setNumero("FAC-ALPHA-1");
        f1.setClient(alpha);
        f1.setDateEmission(LocalDate.of(2025, 1, 1));
        f1.setDateEcheance(LocalDate.of(2025, 1, 10));
        f1.setStatut(StatutFacture.PAYEE);
        f1.setMontantTtc(new BigDecimal("500000"));
        f1.setMontantPaye(new BigDecimal("500000"));
        f1.setMontantRestant(BigDecimal.ZERO);

        Facture f2 = new Facture();
        f2.setNumero("FAC-ALPHA-2");
        f2.setClient(alpha);
        f2.setDateEmission(LocalDate.of(2025, 2, 1));
        f2.setDateEcheance(LocalDate.of(2025, 2, 10));
        f2.setStatut(StatutFacture.PAYEE);
        f2.setMontantTtc(new BigDecimal("1000000"));
        f2.setMontantPaye(new BigDecimal("1000000"));
        f2.setMontantRestant(BigDecimal.ZERO);

        // Beta : 1 facture payée = 600 000 TTC
        Facture f3 = new Facture();
        f3.setNumero("FAC-BETA-1");
        f3.setClient(beta);
        f3.setDateEmission(LocalDate.of(2025, 3, 1));
        f3.setDateEcheance(LocalDate.of(2025, 3, 10));
        f3.setStatut(StatutFacture.PAYEE);
        f3.setMontantTtc(new BigDecimal("600000"));
        f3.setMontantPaye(new BigDecimal("600000"));
        f3.setMontantRestant(BigDecimal.ZERO);

        factureRepository.save(f1);
        factureRepository.save(f2);
        factureRepository.save(f3);

        // WHEN
        List<Object[]> top = factureRepository.findTopClients(5);

        // THEN
        assertFalse(top.isEmpty());
        Object[] first = top.get(0);
        Long clientId = ((Number) first[0]).longValue();
        String clientNom = (String) first[1];
        BigDecimal total = (BigDecimal) first[2];

        assertEquals(alpha.getId(), clientId);
        assertEquals("Alpha", clientNom);
        assertEquals(new BigDecimal("1500000.00"), total);
    }
}
