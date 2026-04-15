package com.unishare.api.modules.user.dto;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class UserProfileResponse {
    private UUID userId;
    private String firstName;
    private String lastName;
    private String headline;
    private UUID avatarFileId;
    private String bio;
    private String location;
    private Instant createdAt;
    private Instant updatedAt;
}
