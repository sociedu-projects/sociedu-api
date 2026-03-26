package com.unishare.api.modules.user.dto;

import lombok.Data;

@Data
public class UserLanguageResponse {
    private Long id;
    private Long userId;
    private String language;
    private String level;
}
