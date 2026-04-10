package com.unishare.api.modules.profile.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
public class MentorOnboardingDto {

    // ======================== POST /mentor/apply ========================

    @Data
    public static class MentorApplyRequest {
        @Size(max = 255, message = "Headline must not exceed 255 characters")
        private String headline;

        private String expertise;
    }

    // ======================== Response chung cho mentor application ========================

    @Data
    @Builder
    public static class MentorApplicationResponse {
        private Long userId;
        private String headline;
        private String expertise;
        private BigDecimal basePrice;
        private Float ratingAvg;
        private Integer sessionsCompleted;
        private String verificationStatus;
        private Instant createdAt;
        private Instant updatedAt;
    }

    // ======================== PATCH /mentor/me/profile ========================

    @Data
    public static class UpdateMentorProfileRequest {
        @Size(max = 255, message = "Headline must not exceed 255 characters")
        private String headline;

        private String expertise;

        @DecimalMin(value = "0.0", inclusive = true, message = "Base price must be >= 0")
        private BigDecimal basePrice;
    }

    // ======================== GET /mentor/me/verification-status ========================

    @Data
    @Builder
    public static class VerificationStatusResponse {
        private String currentStatus;
        private Instant lastUpdatedAt;
        private List<MentorReviewHistoryResponse> reviewHistory;
    }

    // ======================== Verification Documents ========================

    @Data
    @Builder
    public static class VerificationDocumentResponse {
        private Long id;
        private String fileUrl;
        private String fileName;
        private String fileType;
        private Long fileSize;
        private String status;
        private String note;
        private Instant createdAt;
    }

    // ======================== Payout Info ========================

    @Data
    public static class PayoutInfoRequest {
        @NotBlank(message = "Bank name is required")
        private String bankName;

        @NotBlank(message = "Account number is required")
        private String accountNumber;

        @NotBlank(message = "Account holder is required")
        private String accountHolder;

        private String branch;
    }

    @Data
    @Builder
    public static class PayoutInfoResponse {
        private String bankName;
        private String maskedAccountNumber;
        private String accountHolder;
        private String branch;
        private Instant updatedAt;
    }

    // ======================== Review History ========================

    @Data
    @Builder
    public static class MentorReviewHistoryResponse {
        private String fromStatus;
        private String toStatus;
        private String reasonCode;
        private String note;
        private Instant createdAt;
    }
}
