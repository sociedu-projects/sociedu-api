package com.unishare.api.modules.trust.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;
@Data
public class CreateModerationReportRequest {

    @NotBlank
    private String type;

    @NotNull
    private UUID entityId;

    private UUID reportedUserId;

    @NotBlank
    private String reason;

    private String description;
}
