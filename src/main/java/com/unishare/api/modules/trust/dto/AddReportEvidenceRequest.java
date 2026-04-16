package com.unishare.api.modules.trust.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class AddReportEvidenceRequest {

    @NotNull
    private UUID fileId;

    private String description;
}
