package com.unishare.api.modules.user.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserCertificateResponse {
    private Long id;
    private Long userId;
    private String name;
    private String organization;
    private LocalDate issueDate;
    private LocalDate expirationDate;
    private String credentialUrl;
}
