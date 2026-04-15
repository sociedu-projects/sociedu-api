package com.unishare.api.modules.trust.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class DisputeResponse {
    private Long id;
    private Long reportId;
    private Long bookingId;
    private Long sessionId;
    private Long raisedBy;
    private String reason;
    private String description;
    private String status;
    private String resolutionNote;
    private Instant createdAt;
    private Instant resolvedAt;
    private Long resolvedBy;
}
