package com.unishare.api.modules.service.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateServicePackageVersionRequest {

    @NotNull(message = "Giá version không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Giá version phải lớn hơn hoặc bằng 0")
    private BigDecimal price;

    @NotNull(message = "Thời lượng version không được để trống")
    @Min(value = 1, message = "Thời lượng version phải lớn hơn hoặc bằng 1")
    private Integer duration;

    private String deliveryType;
}
