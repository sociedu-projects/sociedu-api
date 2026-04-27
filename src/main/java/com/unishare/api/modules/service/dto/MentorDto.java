package com.unishare.api.modules.service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class MentorDto {

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
        private List<CurriculumItemResponse> curriculums;
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
        @NotBlank(message = "Tiêu đề curriculum không được để trống")
        private String title;
        private String description;

        @NotNull(message = "Thứ tự curriculum không được để trống")
        @Min(value = 1, message = "Thứ tự curriculum phải lớn hơn hoặc bằng 1")
        private Integer orderIndex;

        @NotNull(message = "Thời lượng curriculum không được để trống")
        @Min(value = 1, message = "Thời lượng curriculum phải lớn hơn hoặc bằng 1")
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
