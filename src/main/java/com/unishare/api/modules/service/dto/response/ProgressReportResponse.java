package com.unishare.api.modules.service.dto.response;

import com.unishare.api.modules.service.entity.ReportStatus;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ProgressReportResponse {
    private UUID id;
    private UUID menteeId;
    private UUID mentorId;

    // Additional fields we could populate loosely
    private String menteeName;
    private String mentorName;

    private String title;
    private String content;
    private String attachmentUrl;
    private ReportStatus status;
    private String mentorFeedback;
    private Instant createdAt;
    private Instant updatedAt;
}
