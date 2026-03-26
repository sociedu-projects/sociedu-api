package com.unishare.api.modules.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileRequest {
    @Size(max = 255)
    private String fullName;
    private String avatarUrl;
    private String bio;
}
