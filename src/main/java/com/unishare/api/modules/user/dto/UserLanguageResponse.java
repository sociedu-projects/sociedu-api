package com.unishare.api.modules.user.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UserLanguageResponse {
    private UUID id;
    private UUID userId;
    private String language;
    private String level;
}
