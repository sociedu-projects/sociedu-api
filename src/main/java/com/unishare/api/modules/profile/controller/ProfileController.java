package com.unishare.api.modules.profile.controller;

import com.unishare.api.common.dto.ApiResponse;
import com.unishare.api.infrastructure.security.CustomUserPrincipal;
import com.unishare.api.modules.profile.dto.ProfileDto.AvatarUploadResponse;
import com.unishare.api.modules.profile.dto.ProfileDto.MyProfileResponse;
import com.unishare.api.modules.profile.dto.ProfileDto.UpdateProfileRequest;
import com.unishare.api.modules.profile.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Endpoints quản lý hồ sơ cá nhân — dùng chung cho Buyer và Mentor.
 */
@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    /**
     * Xem hồ sơ đầy đủ của chính mình.
     * Bao gồm: profile cơ bản + educations + languages + experiences + certificates.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MyProfileResponse>> getMyProfile(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.<MyProfileResponse>build()
                .withData(profileService.getMyProfile(principal.getUserId()))
                .withMessage("Success"));
    }

    /**
     * Cập nhật thông tin cơ bản (fullName, bio). Partial update.
     */
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<MyProfileResponse>> updateMyProfile(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.<MyProfileResponse>build()
                .withData(profileService.updateMyProfile(principal.getUserId(), request))
                .withMessage("Profile updated successfully"));
    }

    /**
     * Upload avatar. Chỉ chấp nhận JPEG, PNG, WebP. Tối đa 5MB.
     */
    @PostMapping("/avatar")
    public ResponseEntity<ApiResponse<AvatarUploadResponse>> uploadAvatar(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.<AvatarUploadResponse>build()
                .withData(profileService.uploadAvatar(principal.getUserId(), file))
                .withMessage("Avatar uploaded successfully"));
    }
}
