package com.unishare.api.modules.auth.repository;

import com.unishare.api.modules.auth.entity.OtpToken;
import com.unishare.api.modules.auth.entity.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    Optional<OtpToken> findTopByUserIdAndTypeAndUsedFalseOrderByCreatedAtDesc(
            Long userId, OtpType type);
}
