package com.unishare.api.modules.profile.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

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
    }
}
