package com.unishare.api.modules.trust.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateModerationReportRequest {

    @NotBlank
    private String type;

    @NotNull
    private Long entityId;

    private Long reportedUserId;

    @NotBlank
    private String reason;

    private String description;
}
