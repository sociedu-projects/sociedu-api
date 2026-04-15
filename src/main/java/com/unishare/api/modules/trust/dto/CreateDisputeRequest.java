package com.unishare.api.modules.trust.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateDisputeRequest {

    private Long reportId;

    private Long bookingId;

    private Long sessionId;

    @NotBlank
    private String reason;

    private String description;
}
