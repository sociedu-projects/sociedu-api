package com.unishare.api.modules.mentor.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MentorRequest {

    @NotBlank(message = "Headline không được để trống")
    private String headline;

    @NotBlank(message = "Expertise không được để trống")
    private String expertise;

    @NotNull(message = "Base price không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Base price phải lớn hơn hoặc bằng 0")
    private BigDecimal basePrice;
}
