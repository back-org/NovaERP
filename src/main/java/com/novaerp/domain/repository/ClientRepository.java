
package com.novaerp.domain.repository;

import com.novaerp.domain.entity.client.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
}