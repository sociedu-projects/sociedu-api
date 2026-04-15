package com.unishare.api.modules.trust.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResolveReportRequest {

    @NotBlank
    private String status;

    @NotBlank
    private String resolutionNote;
}
