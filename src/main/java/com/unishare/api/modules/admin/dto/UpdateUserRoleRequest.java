package com.unishare.api.modules.admin.dto;

import com.unishare.api.common.constants.Roles;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateUserRoleRequest {

    @NotBlank(message = "Role must not be blank")
    @Pattern(regexp = Roles.API_PRINCIPAL_ASSIGNABLE_PATTERN, message = "Role must be USER, MENTOR, or ADMIN")
    private String role;
}
