package com.novaerp.domain.repository;

import com.novaerp.domain.entity.user.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {
    Optional<BlacklistedToken> findByToken(String token);
    void deleteByExpiryDateBefore(java.time.Instant now);
}
