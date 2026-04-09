package com.unishare.api.modules.mentor.controller;

import com.unishare.api.common.dto.ApiResponse;
import com.unishare.api.infrastructure.security.CustomUserPrincipal;
import com.unishare.api.modules.mentor.dto.MentorDto.*;
import com.unishare.api.modules.mentor.service.MentorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/mentors")
@RequiredArgsConstructor
public class MentorController {

    private final MentorService mentorService;

    // ===================== PUBLIC ENDPOINTS =====================

    /**
     * Lấy danh sách tất cả mentor đã được xác minh (verified).
     * Endpoint công khai - ai cũng có thể xem danh sách mentor.
     * GET /api/v1/mentors
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<MentorProfileResponse>>> getAllVerifiedMentors() {
        return ResponseEntity.ok(ApiResponse.<List<MentorProfileResponse>>build()
                .withData(mentorService.getAllVerifiedMentors()));
    }

    /**
     * Lấy thông tin hồ sơ chi tiết của một mentor theo ID.
     * Endpoint công khai - dùng để xem trang profile của mentor.
     * GET /api/v1/mentors/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MentorProfileResponse>> getMentorProfile(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<MentorProfileResponse>build()
                .withData(mentorService.getMentorProfile(id)));
    }

    /**
     * Lấy danh sách các gói dịch vụ (service packages) của một mentor theo ID.
     * Endpoint công khai - dùng để xem các gói mentoring mà mentor cung cấp.
     * GET /api/v1/mentors/{id}/packages
     */
    @GetMapping("/{id}/packages")
    public ResponseEntity<ApiResponse<List<ServicePackageResponse>>> getMentorPackages(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<List<ServicePackageResponse>>build()
                .withData(mentorService.getMentorPackages(id)));
    }

    // ===================== MENTOR SELF-MANAGEMENT ENDPOINTS =====================

    /**
     * Cập nhật (hoặc tạo mới) hồ sơ mentor của chính người dùng đang đăng nhập.
     * Yêu cầu xác thực - chỉ mentor tự cập nhật profile của mình.
     * PUT /api/v1/mentors/me
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<MentorProfileResponse>> updateMyProfile(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody MentorProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.<MentorProfileResponse>build()
                .withData(mentorService.createOrUpdateProfile(principal.getUserId(), request)));
    }

    /**
     * Thêm một gói dịch vụ mới cho mentor đang đăng nhập.
     * Yêu cầu xác thực - mentor tự tạo gói dịch vụ của mình.
     * POST /api/v1/mentors/me/packages
     */
    @PostMapping("/me/packages")
    public ResponseEntity<ApiResponse<ServicePackageResponse>> addPackage(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody ServicePackageRequest request) {
        return ResponseEntity.ok(ApiResponse.<ServicePackageResponse>build()
                .withData(mentorService.createPackage(principal.getUserId(), request)));
    }

    /**
     * Xoá một gói dịch vụ của mentor đang đăng nhập theo package ID.
     * Yêu cầu xác thực - mentor chỉ có thể xoá gói dịch vụ của chính mình.
     * DELETE /api/v1/mentors/me/packages/{pkgId}
     */
    @DeleteMapping("/me/packages/{pkgId}")
    public ResponseEntity<ApiResponse<Void>> deletePackage(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long pkgId) {
        mentorService.deletePackage(principal.getUserId(), pkgId);
        return ResponseEntity.ok(ApiResponse.<Void>build().withMessage("Package deleted"));
    }
}
