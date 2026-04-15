package com.unishare.api.modules.service.dto.request;

import com.unishare.api.modules.service.entity.ReportStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewReportRequest {
    @NotNull(message = "Trạng thái không được để trống")
    private ReportStatus status;

    @NotBlank(message = "Nhận xét không được để trống")
    private String mentorFeedback;
}
