package com.unishare.api.modules.service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateServicePackageRequest {

    @NotBlank(message = "Tên gói dịch vụ không được để trống")
    private String name;

    private String description;

    @NotNull(message = "Giá gói dịch vụ không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Giá gói dịch vụ phải lớn hơn hoặc bằng 0")
    private BigDecimal price;

    @NotNull(message = "Thời lượng gói dịch vụ không được để trống")
    @Min(value = 1, message = "Thời lượng gói dịch vụ phải lớn hơn hoặc bằng 1")
    private Integer duration;

    private String deliveryType;

    @Valid
    @NotEmpty(message = "Danh sách curriculum không được để trống")
    private List<CurriculumRequest> curriculums;

    @Data
    public static class CurriculumRequest {

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
}
