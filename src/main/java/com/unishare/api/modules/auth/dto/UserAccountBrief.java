package com.unishare.api.modules.auth.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Ảnh chụp tài khoản (identity + roles) cho module admin — không kèm profile user. */
public record UserAccountBrief(
        UUID userId,
        String email,
        String status,
        Instant createdAt,
        List<String> roles
) {
}
