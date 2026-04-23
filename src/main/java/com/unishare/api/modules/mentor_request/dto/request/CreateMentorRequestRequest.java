package com.unishare.api.modules.mentor_request.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Request body cho {@code POST /api/v1/mentor-requests} (submit + resubmit).
 */
@Data
public class CreateMentorRequestRequest {

    @NotBlank(message = "Headline không được trống")
    @Size(max = 255)
    private String headline;

    @NotBlank(message = "Mô tả bản thân không được trống")
    @Size(min = 50, max = 5000, message = "Mô tả bản thân từ 50 đến 5000 ký tự")
    private String bio;

    @NotEmpty(message = "Cần ít nhất 1 lĩnh vực chuyên môn")
    @Size(max = 20)
    private List<@NotBlank @Size(max = 64) String> expertise;

    @NotNull
    @Min(value = 0, message = "Số năm kinh nghiệm không âm")
    @Max(value = 70)
    private Integer yearsOfExperience;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal hourlyRate;

    /** Chọn 1 trong 2: upload file rồi gửi fileId, hoặc link URL ngoài. */
    private UUID cvFileId;

    @Size(max = 1024)
    private String cvUrl;

    @Size(max = 10)
    private List<@Size(max = 1024) String> portfolioUrls;

    @Size(max = 20)
    private List<CertificateInput> certificates;

    @Data
    public static class CertificateInput {
        @NotBlank
        @Size(max = 255)
        private String name;

        @Size(max = 255)
        private String issuer;

        @Min(1900)
        @Max(2100)
        private Integer year;

        @Size(max = 1024)
        private String url;
    }
}
