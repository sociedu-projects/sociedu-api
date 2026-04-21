package com.unishare.api.modules.service.controller;

import com.unishare.api.common.dto.ApiResponse;
import com.unishare.api.config.OpenApiConfig;
import com.unishare.api.infrastructure.security.CustomUserPrincipal;
import com.unishare.api.modules.service.dto.MentorDto.CurriculumItemRequest;
import com.unishare.api.modules.service.dto.MentorDto.CurriculumItemResponse;
import com.unishare.api.modules.service.dto.MentorDto.ServicePackageResponse;
import com.unishare.api.modules.service.dto.request.CreateServicePackageVersionRequest;
import com.unishare.api.modules.service.dto.request.UpdateServicePackageRequest;
import com.unishare.api.modules.service.service.MentorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/service-packages")
@RequiredArgsConstructor
@Tag(name = "Service packages")
@SecurityRequirements(value = {})
public class ServicePackageController {

    private final MentorService mentorService;

    @Operation(summary = "Danh sach goi dich vu dang mo")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ServicePackageResponse>>> getActivePackages(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.<Page<ServicePackageResponse>>build()
                .withData(mentorService.getActivePackages(pageable)));
    }

    @Operation(summary = "Chi tiet goi dich vu dang mo")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServicePackageResponse>> getActivePackage(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.<ServicePackageResponse>build()
                .withData(mentorService.getActivePackage(id)));
    }

    @Operation(summary = "Tao version moi cho goi dich vu cua mentor")
    @SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
    @PreAuthorize("hasRole('MENTOR')")
    @PostMapping("/{id}/versions")
    public ResponseEntity<ApiResponse<ServicePackageResponse>> createPackageVersion(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody CreateServicePackageVersionRequest request) {
        return ResponseEntity.ok(ApiResponse.<ServicePackageResponse>build()
                .withData(mentorService.createPackageVersion(principal.getUserId(), id, request))
                .withMessage("Tao version goi dich vu thanh cong"));
    }

    @Operation(summary = "Cap nhat curriculum trong mot version cua goi dich vu")
    @SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
    @PreAuthorize("hasRole('MENTOR')")
    @PutMapping("/{id}/versions/{versionId}/curriculums/{curriculumId}")
    public ResponseEntity<ApiResponse<CurriculumItemResponse>> updateCurriculum(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID id,
            @PathVariable UUID versionId,
            @PathVariable UUID curriculumId,
            @Valid @RequestBody CurriculumItemRequest request) {
        return ResponseEntity.ok(ApiResponse.<CurriculumItemResponse>build()
                .withData(mentorService.updateCurriculumItem(principal.getUserId(), id, versionId, curriculumId, request))
                .withMessage("Cap nhat curriculum thanh cong"));
    }

    @Operation(summary = "Cap nhat goi dich vu cua mentor")
    @SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
    @PreAuthorize("hasRole('MENTOR')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ServicePackageResponse>> updatePackage(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateServicePackageRequest request) {
        return ResponseEntity.ok(ApiResponse.<ServicePackageResponse>build()
                .withData(mentorService.updatePackage(principal.getUserId(), id, request))
                .withMessage("Cap nhat goi dich vu thanh cong"));
    }

    @Operation(summary = "Bat hoac tat goi dich vu cua mentor")
    @SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
    @PreAuthorize("hasRole('MENTOR')")
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<ServicePackageResponse>> togglePackage(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.<ServicePackageResponse>build()
                .withData(mentorService.togglePackage(principal.getUserId(), id))
                .withMessage("Cap nhat trang thai goi dich vu thanh cong"));
    }
}
