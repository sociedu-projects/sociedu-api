package com.unishare.api.modules.service.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class MentorDto {

    @Data
    public static class MentorProfileRequest {
        private String headline;
        private String expertise;
        private BigDecimal basePrice;
    }

    @Data
    @Builder
    public static class MentorProfileResponse {
        private UUID userId;
        private String headline;
        private String expertise;
        private BigDecimal basePrice;
        private Float ratingAvg;
        private Integer sessionsCompleted;
        private String verificationStatus;
        private List<ServicePackageResponse> packages;
    }

    @Data
    public static class ServicePackageRequest {
        private String name;
        private String description;
        private Integer duration;
        private BigDecimal price;
        private String deliveryType;
    }

    @Data
    @Builder
    public static class ServicePackageVersionResponse {
        private UUID id;
        private BigDecimal price;
        private Integer duration;
        private String deliveryType;
        private Boolean isDefault;
    }

    @Data
    @Builder
    public static class ServicePackageResponse {
        private UUID id;
        private UUID mentorId;
        private String name;
        private String description;
        private Boolean isActive;
        private List<ServicePackageVersionResponse> versions;
    }

    @Data
    public static class CurriculumItemRequest {
        private String title;
        private String description;
        private Integer orderIndex;
        private Integer duration;
    }

    @Data
    @Builder
    public static class CurriculumItemResponse {
        private UUID id;
        private UUID packageVersionId;
        private String title;
        private String description;
        private Integer orderIndex;
        private Integer duration;
    }
}
