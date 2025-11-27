package com.novaerp.domain.entity.client;

import com.novaerp.domain.entity.factures.Facture;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false, unique = true)
    private String email;

    private String telephone;

    private String adresse;

    @Column(nullable = false)
    private Boolean actif = true;

    @OneToMany(mappedBy = "client")
    private List<Facture> factures = new ArrayList<>();

    // getters / setters
}
