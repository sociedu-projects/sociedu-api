package com.unishare.api.modules.admin.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.unishare.api.modules.user.dto.UserProfileNames;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response riêng admin: ghép {@link UserProfileNames} (user) + metadata tài khoản; JSON vẫn phẳng nhờ {@link JsonUnwrapped}.
 */
@Data
@Builder
public class AdminUserSummaryResponse {

    private UUID userId;
    private String email;

    @JsonUnwrapped
    private UserProfileNames profile;

    private String status;
    private Instant createdAt;
    private List<String> roles;
}
