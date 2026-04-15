package com.unishare.api.modules.booking.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class EvidenceResponse {
    private UUID id;
    private UUID uploadedBy;
    private UUID fileId;
    private String description;
    private Instant createdAt;
}
