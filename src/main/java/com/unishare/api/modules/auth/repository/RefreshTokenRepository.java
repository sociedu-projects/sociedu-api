package com.unishare.api.modules.auth.repository;

import com.unishare.api.modules.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenAndRevokedFalse(String token);

    Optional<RefreshToken> findByToken(String token);

    /** Danh sách session còn hiệu lực (chưa revoke, chưa replace, chưa expire). */
    @Query("""
            SELECT rt FROM RefreshToken rt
            WHERE rt.userId = :userId
              AND rt.revoked = false
              AND rt.replacedById IS NULL
              AND rt.expiresAt > :now
            ORDER BY rt.lastUsedAt DESC
            """)
    List<RefreshToken> findActiveSessionsByUserId(UUID userId, Instant now);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.userId = :userId")
    void revokeAllByUserId(UUID userId);
}
