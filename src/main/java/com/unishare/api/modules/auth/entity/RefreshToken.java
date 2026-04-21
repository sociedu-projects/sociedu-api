package com.unishare.api.modules.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_tokens_user", columnList = "user_id"),
        @Index(name = "idx_refresh_tokens_token", columnList = "token", unique = true)
})
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

    /** Thời điểm sử dụng gần nhất (mỗi lần refresh hoặc list session cập nhật). */
    @Column(name = "last_used_at")
    private Instant lastUsedAt = Instant.now();

    /** ID của token mới thay thế khi rotation — dùng cho reuse-detection. */
    @Column(name = "replaced_by_id")
    private UUID replacedById;

    /** Thông tin thiết bị ngắn gọn (Browser + OS) cho UI quản lý phiên. */
    @Column(name = "device_info", length = 255)
    private String deviceInfo;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    public static RefreshToken of(UUID userId, String token, Instant expiresAt) {
        RefreshToken rt = new RefreshToken();
        rt.setUserId(userId);
        rt.setToken(token);
        rt.setExpiresAt(expiresAt);
        return rt;
    }

    public static RefreshToken of(UUID userId, String token, Instant expiresAt,
                                  String ipAddress, String userAgent, String deviceInfo) {
        RefreshToken rt = of(userId, token, expiresAt);
        rt.setIpAddress(ipAddress);
        rt.setUserAgent(userAgent);
        rt.setDeviceInfo(deviceInfo);
        return rt;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /** Đang còn hiệu lực dùng được: chưa revoke, chưa hết hạn, chưa bị rotate. */
    public boolean isActive() {
        return !Boolean.TRUE.equals(revoked) && !isExpired() && replacedById == null;
    }
}
