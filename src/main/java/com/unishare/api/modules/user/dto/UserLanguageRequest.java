package com.unishare.api.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLanguageRequest {
    @NotBlank
    private String language;
    
    @NotBlank
    private String level;
}
