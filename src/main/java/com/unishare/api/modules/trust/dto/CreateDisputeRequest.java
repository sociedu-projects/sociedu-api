package com.unishare.api.modules.trust.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.UUID;

@Data
public class CreateDisputeRequest {

    private UUID reportId;

    private UUID bookingId;

    private UUID sessionId;

    @NotBlank
    private String reason;

    private String description;
}
