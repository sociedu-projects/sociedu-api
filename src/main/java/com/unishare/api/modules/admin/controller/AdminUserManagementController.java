package com.unishare.api.modules.admin.controller;

import com.unishare.api.common.dto.ApiResponse;
import com.unishare.api.config.OpenApiConfig;
import com.unishare.api.modules.admin.dto.AdminUserSummaryResponse;
import com.unishare.api.modules.admin.dto.UpdateUserRoleRequest;
import com.unishare.api.modules.admin.service.AdminUserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
@Tag(name = "Admin - Users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserManagementController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminUserSummaryResponse>>> listUsers() {
        return ResponseEntity.ok(ApiResponse.<List<AdminUserSummaryResponse>>build()
                .withData(adminUserService.listUsers()));
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<ApiResponse<AdminUserSummaryResponse>> updateUserRole(
            @PathVariable("id") UUID userId,
            @Valid @RequestBody UpdateUserRoleRequest request) {
        return ResponseEntity.ok(ApiResponse.<AdminUserSummaryResponse>build()
                .withData(adminUserService.updateUserRole(userId, request.getRole()))
                .withMessage("User role updated successfully"));
    }
}
