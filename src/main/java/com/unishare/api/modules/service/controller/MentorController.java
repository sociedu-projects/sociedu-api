package com.unishare.api.modules.service.controller;

import com.unishare.api.common.dto.ApiResponse;
import com.unishare.api.config.OpenApiConfig;
import com.unishare.api.infrastructure.security.CustomUserPrincipal;
import com.unishare.api.modules.service.dto.MentorDto.CurriculumItemRequest;
import com.unishare.api.modules.service.dto.MentorDto.CurriculumItemResponse;
import com.unishare.api.modules.service.dto.MentorDto.MentorProfileRequest;
import com.unishare.api.modules.service.dto.MentorDto.MentorProfileResponse;
import com.unishare.api.modules.service.dto.MentorDto.ServicePackageResponse;
import com.unishare.api.modules.service.dto.request.CreateServicePackageRequest;
import com.unishare.api.modules.service.service.MentorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/mentors")
@RequiredArgsConstructor
@Tag(name = "Mentors & service packages")
public class MentorController {

    private final MentorService mentorService;

    @Operation(summary = "Danh sach mentor da xac minh")
    @SecurityRequirements(value = {})
    @GetMapping
    public ResponseEntity<ApiResponse<Page<MentorProfileResponse>>> getAllVerifiedMentors(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.<Page<MentorProfileResponse>>build()
                .withData(mentorService.getAllVerifiedMentors(pageable)));
    }

    @Operation(summary = "Chi tiet mentor")
    @PermitAll
    @SecurityRequirements(value = {})
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MentorProfileResponse>> getMentorProfile(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.<MentorProfileResponse>build()
                .withData(mentorService.getMentorProfile(id)));
    }

    @Operation(summary = "Goi dich vu dang mo cua mentor")
    @PermitAll
    @SecurityRequirements(value = {})
    @GetMapping("/{id}/packages")
    public ResponseEntity<ApiResponse<Page<ServicePackageResponse>>> getMentorPackages(
            @PathVariable UUID id,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.<Page<ServicePackageResponse>>build()
                .withData(mentorService.getMentorPackages(id, pageable)));
    }

    @Operation(summary = "Danh sach goi dich vu cua toi")
    @SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
    @PreAuthorize("hasRole('MENTOR')")
    @GetMapping("/me/packages")
    public ResponseEntity<ApiResponse<Page<ServicePackageResponse>>> getMyPackages(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.<Page<ServicePackageResponse>>build()
                .withData(mentorService.getMyPackages(principal.getUserId(), pageable)));
    }

    @Operation(summary = "Cap nhat ho so mentor (me)")
    @SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
    @PreAuthorize("hasRole('MENTOR')")
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<MentorProfileResponse>> updateMyProfile(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody MentorProfileRequest request) {
        MentorProfileResponse resp = mentorService.createOrUpdateProfile(principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.<MentorProfileResponse>build()
                .withData(resp));
    }

    @Operation(summary = "Tao goi dich vu")
    @SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
    @PreAuthorize("hasRole('MENTOR')")
    @PostMapping("/me/packages")
    public ResponseEntity<ApiResponse<ServicePackageResponse>> addPackage(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody CreateServicePackageRequest request) {
        return ResponseEntity.ok(ApiResponse.<ServicePackageResponse>build()
                .withData(mentorService.createPackage(principal.getUserId(), request)));
    }

    @Operation(summary = "Xoa goi dich vu")
    @SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
    @PreAuthorize("hasRole('MENTOR')")
    @DeleteMapping("/me/packages/{pkgId}")
    public ResponseEntity<ApiResponse<Void>> deletePackage(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID pkgId) {
        mentorService.deletePackage(principal.getUserId(), pkgId);
        return ResponseEntity.ok(ApiResponse.<Void>build().withMessage("Package deleted"));
    }

    @Operation(summary = "Them muc curriculum cho phien ban goi")
    @SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
    @PreAuthorize("hasRole('MENTOR')")
    @PostMapping("/me/packages/{pkgId}/versions/{verId}/curriculums")
    public ResponseEntity<ApiResponse<CurriculumItemResponse>> addCurriculum(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID pkgId,
            @PathVariable UUID verId,
            @Valid @RequestBody CurriculumItemRequest request) {
        return ResponseEntity.ok(ApiResponse.<CurriculumItemResponse>build()
                .withData(mentorService.addCurriculumItem(principal.getUserId(), pkgId, verId, request)));
    }

    @Operation(summary = "Liet ke curriculum theo phien ban goi")
    @SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
    @PreAuthorize("hasRole('MENTOR')")
    @GetMapping("/me/packages/{pkgId}/versions/{verId}/curriculums")
    public ResponseEntity<ApiResponse<Page<CurriculumItemResponse>>> listCurriculum(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID pkgId,
            @PathVariable UUID verId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.<Page<CurriculumItemResponse>>build()
                .withData(mentorService.listCurriculum(principal.getUserId(), pkgId, verId, pageable)));
    }

    @Operation(summary = "Xoa muc curriculum")
    @SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
    @PreAuthorize("hasRole('MENTOR')")
    @DeleteMapping("/me/curriculums/{curriculumId}")
    public ResponseEntity<ApiResponse<Void>> deleteCurriculum(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID curriculumId) {
        mentorService.deleteCurriculumItem(principal.getUserId(), curriculumId);
        return ResponseEntity.ok(ApiResponse.<Void>build().withMessage("Da xoa muc curriculum"));
    }
}
