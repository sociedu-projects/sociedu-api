package com.unishare.api.modules.mentor.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class MentorDto {

    // ======================== MENTOR PROFILE ========================

    @Data
    public static class MentorProfileRequest {
        @NotBlank(message = "Headline is required")
        @Size(max = 255, message = "Headline must not exceed 255 characters")
        private String headline;

        @NotBlank(message = "Expertise is required")
        private String expertise;

        @NotNull(message = "Base price is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Base price must be >= 0")
        private BigDecimal basePrice;
    }

    @Data
    @Builder
    public static class MentorProfileResponse {
        private Long userId;
        private String fullName;
        private String avatarUrl;
        private String bio;
        private String headline;
        private String expertise;
        private BigDecimal basePrice;
        private Float ratingAvg;
        private Integer sessionsCompleted;
        private String verificationStatus;
        private List<ServicePackageResponse> packages;
        private List<AvailabilitySlotResponse> availabilityPreview;
    }

    @Data
    @Builder
    public static class MentorListResponse {
        private Long userId;
        private String fullName;
        private String avatarUrl;
        private String headline;
        private String expertise;
        private BigDecimal basePrice;
        private Float ratingAvg;
        private Integer sessionsCompleted;
        private List<ServicePackageResponse> previewPackages;
    }

    @Data
    public static class MentorSearchRequest {
        private String keyword;
        private String expertise;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private String sortBy; // relevance, rating, price_asc, price_desc, sessions, nearest_slot
    }

    // ======================== SERVICE PACKAGES ========================

    @Data
    public static class ServicePackageRequest {
        @NotBlank(message = "Package name is required")
        @Size(max = 255, message = "Package name must not exceed 255 characters")
        private String name;

        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        private String description;

        @NotNull(message = "Duration is required")
        @Min(value = 1, message = "Duration must be at least 1 minute")
        private Integer duration;

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Price must be >= 0")
        private BigDecimal price;

        @NotBlank(message = "Delivery type is required")
        private String deliveryType;
    }

    @Data
    public static class ServicePackageUpdateRequest {
        @Size(max = 255, message = "Package name must not exceed 255 characters")
        private String name;

        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        private String description;

        @Min(value = 1, message = "Duration must be at least 1 minute")
        private Integer duration;

        @DecimalMin(value = "0.0", inclusive = true, message = "Price must be >= 0")
        private BigDecimal price;

        private String deliveryType;
    }

    @Data
    @Builder
    public static class ServicePackageResponse {
        private Long id;
        private Long mentorId;
        private String name;
        private String description;
        private Integer duration;
        private BigDecimal price;
        private String deliveryType;
        private String status;
        private Instant createdAt;
        private Instant updatedAt;
    }

    // ======================== AVAILABILITY SLOTS ========================

    @Data
    public static class AvailabilitySlotRequest {
        @NotNull(message = "Start time is required")
        private Instant startTime;

        @NotNull(message = "End time is required")
        private Instant endTime;

        private String recurrence; // none, daily, weekly

        @Size(max = 100, message = "Timezone must not exceed 100 characters")
        private String timezone;

        @Min(value = 1, message = "Capacity must be at least 1")
        private Integer capacity;
    }

    @Data
    public static class AvailabilitySlotUpdateRequest {
        private Instant startTime;
        private Instant endTime;
        private String status; // blocked, cancelled
        @Min(value = 1, message = "Capacity must be at least 1")
        private Integer capacity;
    }

    @Data
    @Builder
    public static class AvailabilitySlotResponse {
        private Long id;
        private Long mentorId;
        private Instant startTime;
        private Instant endTime;
        private String status;
        private String recurrence;
        private String timezone;
        private Integer capacity;
    }

    // ======================== EXPERTISE CATEGORIES ========================

    @Data
    @Builder
    public static class ExpertiseCategoryResponse {
        private Long id;
        private String name;
        private Long parentId;
        private List<ExpertiseCategoryResponse> children;
    }
}
