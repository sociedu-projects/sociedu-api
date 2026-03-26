package com.unishare.api.modules.user.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class UserProfileResponse {
    private Long userId;
    private String fullName;
    private String avatarUrl;
    private String bio;
    private Instant createdAt;
    private Instant updatedAt;
}
