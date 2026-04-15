package com.unishare.api.modules.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "otp_tokens")
@Getter
@Setter
@NoArgsConstructor
public class OtpToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 128)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OtpType type;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    private Boolean used = false;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    public static OtpToken of(UUID userId, String code, OtpType type, int ttlMinutes) {
        OtpToken otp = new OtpToken();
        otp.setUserId(userId);
        otp.setCode(code);
        otp.setType(type);
        otp.setExpiresAt(Instant.now().plusSeconds(ttlMinutes * 60L));
        return otp;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
