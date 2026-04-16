package com.unishare.api.modules.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class CreateReportRequest {
    @NotNull(message = "ID Mentor không được để trống")
    private UUID mentorId;

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    @NotBlank(message = "Nội dung báo cáo không được để trống")
    private String content;

    private String attachmentUrl;
}
