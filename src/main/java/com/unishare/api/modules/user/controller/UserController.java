package com.unishare.api.modules.user.controller;

import com.unishare.api.common.dto.ApiResponse;
import com.unishare.api.config.OpenApiConfig;
import com.unishare.api.infrastructure.security.CustomUserPrincipal;
import com.unishare.api.modules.user.dto.*;
import com.unishare.api.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
@Tag(name = "Users — Me")
public class UserController {

    private final UserService userService;

    // Profile
    @PreAuthorize("hasAuthority(T(com.unishare.api.common.constants.Capabilities).VIEW_PROFILE)")
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.<UserProfileResponse>build()
                .withData(userService.getProfile(principal.getUserId()))
                .withMessage("Success"));
    }

    @PreAuthorize("hasAuthority(T(com.unishare.api.common.constants.Capabilities).UPDATE_PROFILE)")
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody UserProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.<UserProfileResponse>build()
                .withData(userService.updateProfile(principal.getUserId(), request))
                .withMessage("Profile updated successfully"));
    }

    // Education
    @PreAuthorize("hasAuthority(T(com.unishare.api.common.constants.Capabilities).VIEW_PROFILE)")
    @GetMapping("/educations")
    public ResponseEntity<ApiResponse<List<UserEducationResponse>>> getEducations(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.<List<UserEducationResponse>>build()
                .withData(userService.getEducations(principal.getUserId())));
    }

    @PreAuthorize("hasAuthority(T(com.unishare.api.common.constants.Capabilities).UPDATE_PROFILE)")
    @PostMapping("/educations")
    public ResponseEntity<ApiResponse<UserEducationResponse>> addEducation(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody UserEducationRequest request) {
        return ResponseEntity.ok(ApiResponse.<UserEducationResponse>build()
                .withData(userService.addEducation(principal.getUserId(), request))
                .withMessage("Education added successfully"));
    }

    @PreAuthorize("hasAuthority(T(com.unishare.api.common.constants.Capabilities).UPDATE_PROFILE)")
    @PutMapping("/educations/{id}")
    public ResponseEntity<ApiResponse<UserEducationResponse>> updateEducation(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UserEducationRequest request) {
        return ResponseEntity.ok(ApiResponse.<UserEducationResponse>build()
                .withData(userService.updateEducation(principal.getUserId(), id, request))
                .withMessage("Education updated successfully"));
    }

    @PreAuthorize("hasAuthority(T(com.unishare.api.common.constants.Capabilities).UPDATE_PROFILE)")
    @DeleteMapping("/educations/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEducation(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long id) {
        userService.deleteEducation(principal.getUserId(), id);
        return ResponseEntity.ok(ApiResponse.<Void>build().withMessage("Education deleted successfully"));
    }

    // Language
    @PreAuthorize("hasAuthority(T(com.unishare.api.common.constants.Capabilities).VIEW_PROFILE)")
    @GetMapping("/languages")
    public ResponseEntity<ApiResponse<List<UserLanguageResponse>>> getLanguages(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.<List<UserLanguageResponse>>build()
                .withData(userService.getLanguages(principal.getUserId())));
    }

    @PreAuthorize("hasAuthority(T(com.unishare.api.common.constants.Capabilities).UPDATE_PROFILE)")
    @PostMapping("/languages")
    public ResponseEntity<ApiResponse<UserLanguageResponse>> addLanguage(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody UserLanguageRequest request) {
        return ResponseEntity.ok(ApiResponse.<UserLanguageResponse>build()
                .withData(userService.addLanguage(principal.getUserId(), request))
                .withMessage("Language added successfully"));
    }

    @PreAuthorize("hasAuthority(T(com.unishare.api.common.constants.Capabilities).UPDATE_PROFILE)")
    @PutMapping("/languages/{id}")
    public ResponseEntity<ApiResponse<UserLanguageResponse>> updateLanguage(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UserLanguageRequest request) {
        return ResponseEntity.ok(ApiResponse.<UserLanguageResponse>build()
                .withData(userService.updateLanguage(principal.getUserId(), id, request))
                .withMessage("Language updated successfully"));
    }

    @PreAuthorize("hasAuthority(T(com.unishare.api.common.constants.Capabilities).UPDATE_PROFILE)")
    @DeleteMapping("/languages/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLanguage(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long id) {
        userService.deleteLanguage(principal.getUserId(), id);
        return ResponseEntity.ok(ApiResponse.<Void>build().withMessage("Language deleted successfully"));
    }

    // Experience
    @PreAuthorize("hasAuthority(T(com.unishare.api.common.constants.Capabilities).VIEW_PROFILE)")
    @GetMapping("/experiences")
    public ResponseEntity<ApiResponse<List<UserExperienceResponse>>> getExperiences(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.<List<UserExperienceResponse>>build()
                .withData(userService.getExperiences(principal.getUserId())));
    }

    @PreAuthorize("hasAuthority(T(com.unishare.api.common.constants.Capabilities).UPDATE_PROFILE)")
    @PostMapping("/experiences")
    public ResponseEntity<ApiResponse<UserExperienceResponse>> addExperience(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody UserExperienceRequest request) {
        return ResponseEntity.ok(ApiResponse.<UserExperienceResponse>build()
                .withData(userService.addExperience(principal.getUserId(), request))
                .withMessage("Experience added successfully"));
    }

    @PreAuthorize("hasAuthority(T(com.unishare.api.common.constants.Capabilities).UPDATE_PROFILE)")
    @PutMapping("/experiences/{id}")
    public ResponseEntity<ApiResponse<UserExperienceResponse>> updateExperience(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UserExperienceRequest request) {
        return ResponseEntity.ok(ApiResponse.<UserExperienceResponse>build()
                .withData(userService.updateExperience(principal.getUserId(), id, request))
                .withMessage("Experience updated successfully"));
    }

    @PreAuthorize("hasAuthority(T(com.unishare.api.common.constants.Capabilities).UPDATE_PROFILE)")
    @DeleteMapping("/experiences/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExperience(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long id) {
        userService.deleteExperience(principal.getUserId(), id);
        return ResponseEntity.ok(ApiResponse.<Void>build().withMessage("Experience deleted successfully"));
    }

    // Certificate
    @PreAuthorize("hasAuthority(T(com.unishare.api.common.constants.Capabilities).VIEW_PROFILE)")
    @GetMapping("/certificates")
    public ResponseEntity<ApiResponse<List<UserCertificateResponse>>> getCertificates(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.<List<UserCertificateResponse>>build()
                .withData(userService.getCertificates(principal.getUserId())));
    }

    @PreAuthorize("hasAuthority(T(com.unishare.api.common.constants.Capabilities).UPDATE_PROFILE)")
    @PostMapping("/certificates")
    public ResponseEntity<ApiResponse<UserCertificateResponse>> addCertificate(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody UserCertificateRequest request) {
        return ResponseEntity.ok(ApiResponse.<UserCertificateResponse>build()
                .withData(userService.addCertificate(principal.getUserId(), request))
                .withMessage("Certificate added successfully"));
    }

    @PreAuthorize("hasAuthority(T(com.unishare.api.common.constants.Capabilities).UPDATE_PROFILE)")
    @PutMapping("/certificates/{id}")
    public ResponseEntity<ApiResponse<UserCertificateResponse>> updateCertificate(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UserCertificateRequest request) {
        return ResponseEntity.ok(ApiResponse.<UserCertificateResponse>build()
                .withData(userService.updateCertificate(principal.getUserId(), id, request))
                .withMessage("Certificate updated successfully"));
    }

    @PreAuthorize("hasAuthority(T(com.unishare.api.common.constants.Capabilities).UPDATE_PROFILE)")
    @DeleteMapping("/certificates/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCertificate(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long id) {
        userService.deleteCertificate(principal.getUserId(), id);
        return ResponseEntity.ok(ApiResponse.<Void>build().withMessage("Certificate deleted successfully"));
    }
}
