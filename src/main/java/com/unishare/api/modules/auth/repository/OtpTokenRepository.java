package com.unishare.api.modules.auth.repository;

import com.unishare.api.modules.auth.entity.OtpToken;
import com.unishare.api.modules.auth.entity.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OtpTokenRepository extends JpaRepository<OtpToken, UUID> {

    Optional<OtpToken> findTopByUserIdAndTypeAndUsedFalseOrderByCreatedAtDesc(
            UUID userId, OtpType type);

    Optional<OtpToken> findByCodeAndTypeAndUsedFalse(String code, OtpType type);
}
