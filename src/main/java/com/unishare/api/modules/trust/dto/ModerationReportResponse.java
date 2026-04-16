package com.unishare.api.modules.trust.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ModerationReportResponse {
    private UUID id;
    private UUID reporterId;
    private UUID reportedUserId;
    private String type;
    private UUID entityId;
    private String reason;
    private String description;
    private String status;
    private Instant createdAt;
    private Instant resolvedAt;
    private UUID resolvedBy;
    private String resolutionNote;
}
