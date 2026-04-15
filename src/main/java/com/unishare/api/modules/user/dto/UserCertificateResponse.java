package com.unishare.api.modules.user.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class UserCertificateResponse {
    private UUID id;
    private UUID userId;
    private String name;
    private String organization;
    private LocalDate issueDate;
    private LocalDate expirationDate;
    private UUID credentialFileId;
    private String description;
}
