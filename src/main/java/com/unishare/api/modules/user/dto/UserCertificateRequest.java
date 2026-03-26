package com.unishare.api.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserCertificateRequest {
    @NotBlank
    private String name;
    
    @NotBlank
    private String organization;
    
    private LocalDate issueDate;
    private LocalDate expirationDate;
    private String credentialUrl;
}
