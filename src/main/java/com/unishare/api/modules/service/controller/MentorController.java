package com.unishare.api.modules.service.controller;

import com.unishare.api.common.dto.ApiResponse;
import com.unishare.api.config.OpenApiConfig;
import com.unishare.api.infrastructure.security.CustomUserPrincipal;
import com.unishare.api.modules.service.dto.MentorDto.*;
import com.unishare.api.modules.service.dto.request.CreateServicePackageRequest;
import com.unishare.api.modules.service.service.MentorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/mentors")
@RequiredArgsConstructor
@Tag(name = "Mentors & service packages")
public class MentorController {

    private final MentorService mentorService;

    // Public Mentor Directory
    @Operation(summary = "Danh sách mentor đã xác minh")
    @SecurityRequirements(value = {})
    @GetMapping
    public ResponseEntity<ApiResponse<List<MentorProfileResponse>>> getAllVerifiedMentors() {
        return ResponseEntity.ok(ApiResponse.<List<MentorProfileResponse>>build()
                .withData(mentorService.getAllVerifiedMentors()));
    }

    @Operation(summary = "Chi tiết mentor")
    @PermitAll
    @SecurityRequirements(value = {})
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MentorProfileResponse>> getMentorProfile(@PathVariable java.util.UUID id) {
        return ResponseEntity.ok(ApiResponse.<MentorProfileResponse>build()
                .withData(mentorService.getMentorProfile(id)));
    }

    @Operation(summary = "Gói dịch vụ của mentor")
    @PermitAll
    @SecurityRequirements(value = {})
    @GetMapping("/{id}/packages")
    public ResponseEntity<ApiResponse<List<ServicePackageResponse>>> getMentorPackages(@PathVariable java.util.UUID id) {
        return ResponseEntity.ok(ApiResponse.<List<ServicePackageResponse>>build()
                .withData(mentorService.getMentorPackages(id)));
    }

    // Mentor Management (for the mentor themselves)
    @Operation(summary = "Cập nhật hồ sơ mentor (me)")
    @SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
    @PreAuthorize("hasRole('MENTOR')")
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<MentorProfileResponse>> updateMyProfile(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody MentorProfileRequest request) {
        MentorProfileResponse resp = mentorService.createOrUpdateProfile(principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.<MentorProfileResponse>build()
                .withData(resp));
    }

    @Operation(summary = "Tạo gói dịch vụ")
    @SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
    @PreAuthorize("hasRole('MENTOR')")
    @PostMapping("/me/packages")
    public ResponseEntity<ApiResponse<ServicePackageResponse>> addPackage(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody CreateServicePackageRequest request) {
        return ResponseEntity.ok(ApiResponse.<ServicePackageResponse>build()
                .withData(mentorService.createPackage(principal.getUserId(), request)));
    }

    @Operation(summary = "Xóa gói dịch vụ")
    @SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
    @PreAuthorize("hasRole('MENTOR')")
    @DeleteMapping("/me/packages/{pkgId}")
    public ResponseEntity<ApiResponse<Void>> deletePackage(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable java.util.UUID pkgId) {
        mentorService.deletePackage(principal.getUserId(), pkgId);
        return ResponseEntity.ok(ApiResponse.<Void>build().withMessage("Package deleted"));
    }

    @Operation(summary = "Thêm mục curriculum cho phiên bản gói")
    @SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
    @PreAuthorize("hasRole('MENTOR')")
    @PostMapping("/me/packages/{pkgId}/versions/{verId}/curriculums")
    public ResponseEntity<ApiResponse<CurriculumItemResponse>> addCurriculum(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable java.util.UUID pkgId,
            @PathVariable java.util.UUID verId,
            @Valid @RequestBody CurriculumItemRequest request) {
        return ResponseEntity.ok(ApiResponse.<CurriculumItemResponse>build()
                .withData(mentorService.addCurriculumItem(principal.getUserId(), pkgId, verId, request)));
    }

    @Operation(summary = "Liệt kê curriculum theo phiên bản gói")
    @SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
    @PreAuthorize("hasRole('MENTOR')")
    @GetMapping("/me/packages/{pkgId}/versions/{verId}/curriculums")
    public ResponseEntity<ApiResponse<List<CurriculumItemResponse>>> listCurriculum(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable java.util.UUID pkgId,
            @PathVariable java.util.UUID verId) {
        return ResponseEntity.ok(ApiResponse.<List<CurriculumItemResponse>>build()
                .withData(mentorService.listCurriculum(principal.getUserId(), pkgId, verId)));
    }

    @Operation(summary = "Xóa mục curriculum")
    @SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
    @PreAuthorize("hasRole('MENTOR')")
    @DeleteMapping("/me/curriculums/{curriculumId}")
    public ResponseEntity<ApiResponse<Void>> deleteCurriculum(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable java.util.UUID curriculumId) {
        mentorService.deleteCurriculumItem(principal.getUserId(), curriculumId);
        return ResponseEntity.ok(ApiResponse.<Void>build().withMessage("Đã xóa mục curriculum"));
    }
}
