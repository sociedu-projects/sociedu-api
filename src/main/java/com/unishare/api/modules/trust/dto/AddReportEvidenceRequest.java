package com.unishare.api.modules.trust.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddReportEvidenceRequest {

    @NotNull
    private Long fileId;

    private String description;
}
