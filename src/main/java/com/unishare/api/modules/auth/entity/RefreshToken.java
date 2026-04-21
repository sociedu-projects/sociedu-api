package com.unishare.api.modules.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Refresh token = 1 phiên đăng nhập. Mỗi lần refresh sẽ sinh token mới và
 * set {@code replacedById} của token cũ → cho phép reuse-detection:
 * nếu token đã bị replace mà vẫn được gửi lên, coi là bị đánh cắp → revoke
 * toàn bộ phiên của user.
 */
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

    /** Lần cuối token này được dùng để refresh (hoặc được cấp phát). */
    @Column(name = "last_used_at")
    private Instant lastUsedAt = Instant.now();

    /** ID của refresh token mới thay thế token này sau rotation. */
    @Column(name = "replaced_by_id")
    private UUID replacedById;

    /** Thông tin thiết bị lúc cấp phát (UA parse thô). */
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

    public boolean isActive() {
        return !Boolean.TRUE.equals(revoked) && !isExpired() && replacedById == null;
    }
}
