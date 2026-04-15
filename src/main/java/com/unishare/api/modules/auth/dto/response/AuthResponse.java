package com.unishare.api.modules.auth.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long expiresIn; // seconds

    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private List<String> roles;
}
