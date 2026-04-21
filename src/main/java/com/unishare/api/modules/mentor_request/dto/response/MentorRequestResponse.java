package com.unishare.api.modules.mentor_request.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO cho cả user và admin — chỉ khác ở phần applicant meta.
 * Admin bổ sung {@link #applicant} (email + tên), user nhận {@code null} cho field này.
 */
@Data
@Builder
public class MentorRequestResponse {

    private UUID id;
    private UUID userId;
    private String status;
    private String headline;
    private String bio;
    private List<String> expertise;
    private Integer yearsOfExperience;
    private BigDecimal hourlyRate;
    private UUID cvFileId;
    private String cvUrl;
    private List<String> portfolioUrls;
    private List<CertificateView> certificates;
    private String reason;
    private String note;
    private UUID reviewedBy;
    private Instant reviewedAt;
    private int resubmitCount;
    private Instant createdAt;
    private Instant updatedAt;

    /** Chỉ điền khi trả cho admin. */
    private ApplicantView applicant;

    @Data
    @Builder
    public static class CertificateView {
        private String name;
        private String issuer;
        private Integer year;
        private String url;
    }

    @Data
    @Builder
    public static class ApplicantView {
        private UUID userId;
        private String email;
        private String firstName;
        private String lastName;
        private String fullName;
        private Instant createdAt;
    }
}
