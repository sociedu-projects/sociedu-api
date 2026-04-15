package com.unishare.api.modules.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AddEvidenceRequest {

    @NotNull
    private UUID fileId;

    private String description;
}
