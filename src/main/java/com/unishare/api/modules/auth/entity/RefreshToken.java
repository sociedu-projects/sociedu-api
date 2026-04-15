package com.unishare.api.modules.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, unique = true, length = 512)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    private Boolean revoked = false;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    public static RefreshToken of(UUID userId, String token, Instant expiresAt) {
        RefreshToken rt = new RefreshToken();
        rt.setUserId(userId);
        rt.setToken(token);
        rt.setExpiresAt(expiresAt);
        return rt;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
