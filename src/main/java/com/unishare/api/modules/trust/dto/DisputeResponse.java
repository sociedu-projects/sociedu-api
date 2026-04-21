package com.unishare.api.modules.trust.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class DisputeResponse {
    private UUID id;
    private UUID reportId;
    private UUID bookingId;
    private UUID sessionId;
    private UUID raisedBy;
    private String reason;
    private String description;
    private String status;
    private String resolutionNote;
    private Instant createdAt;
    private Instant resolvedAt;
    private UUID resolvedBy;
}
