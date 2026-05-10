package com.unishare.api.modules.auth.repository;

import com.unishare.api.modules.auth.entity.OtpToken;
import com.unishare.api.modules.auth.entity.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface OtpTokenRepository extends JpaRepository<OtpToken, UUID> {

        Optional<OtpToken> findTopByUserIdAndTypeAndUsedFalseOrderByCreatedAtDesc(
                        UUID userId, OtpType type);

        Optional<OtpToken> findByCodeAndTypeAndUsedFalse(String code, OtpType type);

        /**
         * Đếm số OTP đã tạo cho userId + type kể từ {@code since} — dùng cho rate
         * limit.
         */
        @Query("SELECT COUNT(o) FROM OtpToken o WHERE o.userId = :userId AND o.type = :type AND o.createdAt >= :since")
        long countByUserIdAndTypeAndCreatedAtAfter(
                        @Param("userId") UUID userId,
                        @Param("type") OtpType type,
                        @Param("since") Instant since);

        /**
         * Vô hiệu hoá tất cả OTP cũ chưa dùng cùng type — gọi trước khi tạo OTP mới.
         */
        @Modifying
        @Query("UPDATE OtpToken o SET o.used = true WHERE o.userId = :userId AND o.type = :type AND o.used = false")
        void invalidateAllUnusedByUserIdAndType(
                        @Param("userId") UUID userId,
                        @Param("type") OtpType type);
}
