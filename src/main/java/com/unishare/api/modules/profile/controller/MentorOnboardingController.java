package com.unishare.api.modules.profile.controller;

import com.unishare.api.common.dto.ApiResponse;
import com.unishare.api.infrastructure.security.CustomUserPrincipal;
import com.unishare.api.modules.profile.dto.MentorOnboardingDto.MentorApplyRequest;
import com.unishare.api.modules.profile.dto.MentorOnboardingDto.MentorApplicationResponse;
import com.unishare.api.modules.profile.dto.MentorOnboardingDto.UpdateMentorProfileRequest;
import com.unishare.api.modules.profile.dto.MentorOnboardingDto.VerificationStatusResponse;
import com.unishare.api.modules.profile.service.MentorOnboardingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.unishare.api.modules.profile.dto.MentorOnboardingDto.VerificationDocumentResponse;
import com.unishare.api.modules.profile.dto.MentorOnboardingDto.PayoutInfoRequest;
import com.unishare.api.modules.profile.dto.MentorOnboardingDto.PayoutInfoResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Endpoints cho luồng đăng ký và quản lý hồ sơ mentor (onboarding).
 * Tách biệt với MentorController (public listing) ở /api/v1/mentors.
 */
@RestController
@RequestMapping("/api/v1/mentor")
@RequiredArgsConstructor
public class MentorOnboardingController {

    private final MentorOnboardingService mentorOnboardingService;

    /**
     * Đăng ký làm mentor — tạo hồ sơ mentor draft.
     */
    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<MentorApplicationResponse>> applyMentor(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody MentorApplyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<MentorApplicationResponse>build()
                        .withCode(HttpStatus.CREATED.value())
                        .withData(mentorOnboardingService.applyMentor(principal.getUserId(), request))
                        .withMessage("Mentor application created successfully"));
    }

    /**
     * Xem trạng thái đơn đăng ký mentor.
     */
    @GetMapping("/application/me")
    public ResponseEntity<ApiResponse<MentorApplicationResponse>> getMyApplication(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.<MentorApplicationResponse>build()
                .withData(mentorOnboardingService.getMyApplication(principal.getUserId()))
                .withMessage("Success"));
    }

    /**
     * Cập nhật hồ sơ mentor (headline, expertise, basePrice). Partial update.
     * Chỉ cho phép khi status = draft hoặc rejected.
     */
    @PatchMapping("/me/profile")
    public ResponseEntity<ApiResponse<MentorApplicationResponse>> updateMentorProfile(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody UpdateMentorProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.<MentorApplicationResponse>build()
                .withData(mentorOnboardingService.updateMentorProfile(principal.getUserId(), request))
                .withMessage("Mentor profile updated successfully"));
    }

    /**
     * Xem trạng thái xác minh hiện tại của mentor.
     */
    @GetMapping("/me/verification-status")
    public ResponseEntity<ApiResponse<VerificationStatusResponse>> getVerificationStatus(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.<VerificationStatusResponse>build()
                .withData(mentorOnboardingService.getVerificationStatus(principal.getUserId()))
                .withMessage("Success"));
    }

    /**
     * Upload tài liệu minh chứng xác nhận.
     */
    @PostMapping("/me/verification-documents")
    public ResponseEntity<ApiResponse<VerificationDocumentResponse>> uploadVerificationDocument(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<VerificationDocumentResponse>build()
                        .withData(mentorOnboardingService.uploadVerificationDocument(principal.getUserId(), file))
                        .withMessage("Verification document uploaded successfully"));
    }

    /**
     * Cập nhật thông tin thanh toán cho mentor.
     */
    @PutMapping("/me/payout-info")
    public ResponseEntity<ApiResponse<PayoutInfoResponse>> updatePayoutInfo(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody PayoutInfoRequest request) {
        return ResponseEntity.ok(ApiResponse.<PayoutInfoResponse>build()
                .withData(mentorOnboardingService.updatePayoutInfo(principal.getUserId(), request))
                .withMessage("Payout info updated successfully"));
    }
}
