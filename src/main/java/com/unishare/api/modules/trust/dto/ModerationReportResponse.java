package com.unishare.api.modules.trust.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ModerationReportResponse {
    private Long id;
    private Long reporterId;
    private Long reportedUserId;
    private String type;
    private Long entityId;
    private String reason;
    private String description;
    private String status;
    private Instant createdAt;
    private Instant resolvedAt;
    private Long resolvedBy;
    private String resolutionNote;
}
