package com.unishare.api.modules.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class UserProfileRequest {
    @Size(max = 50)
    private String firstName;
    @Size(max = 50)
    private String lastName;
    @Size(max = 150)
    private String headline;
    private UUID avatarFileId;
    private String bio;
    private String location;
}
