package com.unishare.api.modules.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class UserCertificateRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String organization;

    @NotNull
    private LocalDate issueDate;

    private LocalDate expirationDate;

    /** ID file đã upload (bảng {@code files}), có thể null nếu chưa đính kèm. */
    private UUID credentialFileId;

    private String description;
}
