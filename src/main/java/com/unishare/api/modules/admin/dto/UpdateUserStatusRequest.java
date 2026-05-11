package com.unishare.api.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateUserStatusRequest {

    @NotBlank(message = "Status must not be blank")
    @Pattern(regexp = "(?i)^(active|suspended|pending)$", message = "Status must be active, suspended, or pending")
    private String status;
}
