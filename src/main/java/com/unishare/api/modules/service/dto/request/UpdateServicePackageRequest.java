package com.unishare.api.modules.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateServicePackageRequest {

    @NotBlank(message = "Tên gói dịch vụ không được để trống")
    private String name;

    private String description;
}
