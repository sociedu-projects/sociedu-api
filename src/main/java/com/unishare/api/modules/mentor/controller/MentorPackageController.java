package com.unishare.api.modules.mentor.controller;

import com.unishare.api.common.dto.ApiResponse;
import com.unishare.api.infrastructure.security.CustomUserPrincipal;
import com.unishare.api.modules.mentor.dto.MentorDto.*;
import com.unishare.api.modules.mentor.service.MentorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Mentor self-management endpoints for service packages.
 * Requires MENTOR role.
 */
@RestController
@RequestMapping("/api/v1/mentor/me/packages")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MENTOR')")
public class MentorPackageController {

        private final MentorService mentorService;

        /**
         * Lấy tất cả package của mentor đang đăng nhập (gồm mọi trạng thái).
         * GET /api/v1/mentor/me/packages
         */
        @GetMapping
        public ResponseEntity<ApiResponse<List<ServicePackageResponse>>> getMyPackages(
                        @AuthenticationPrincipal CustomUserPrincipal principal) {
                return ResponseEntity.ok(ApiResponse.<List<ServicePackageResponse>>build()
                                .withData(mentorService.getMyPackages(principal.getUserId())));
        }

        /**
         * Tạo package dịch vụ mới (trạng thái mặc định: draft).
         * POST /api/v1/mentor/me/packages
         */
        @PostMapping
        public ResponseEntity<ApiResponse<ServicePackageResponse>> createPackage(
                        @AuthenticationPrincipal CustomUserPrincipal principal,
                        @Valid @RequestBody ServicePackageRequest request) {
                ServicePackageResponse response = mentorService.createPackage(principal.getUserId(), request);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.<ServicePackageResponse>build()
                                                .withData(response)
                                                .withMessage("Package created successfully"));
        }

        /**
         * Cập nhật thông tin package (partial update).
         * Package active chỉ được sửa field không nhạy cảm (name, description,
         * deliveryType).
         * PATCH /api/v1/mentor/me/packages/{packageId}
         */
        @PatchMapping("/{packageId}")
        public ResponseEntity<ApiResponse<ServicePackageResponse>> updatePackage(
                        @AuthenticationPrincipal CustomUserPrincipal principal,
                        @PathVariable Long packageId,
                        @Valid @RequestBody ServicePackageUpdateRequest request) {
                return ResponseEntity.ok(ApiResponse.<ServicePackageResponse>build()
                                .withData(mentorService.updatePackage(principal.getUserId(), packageId, request)));
        }

        /**
         * Bật/tắt trạng thái package (draft/inactive -> active, active -> inactive).
         * Package inactive không thể được đặt mới.
         * POST /api/v1/mentor/me/packages/{packageId}/activate
         */
        @PostMapping("/{packageId}/activate")
        public ResponseEntity<ApiResponse<ServicePackageResponse>> togglePackageStatus(
                        @AuthenticationPrincipal CustomUserPrincipal principal,
                        @PathVariable Long packageId) {
                return ResponseEntity.ok(ApiResponse.<ServicePackageResponse>build()
                                .withData(mentorService.togglePackageStatus(principal.getUserId(), packageId)));
        }

        /**
         * Xoá package (chỉ khi ở trạng thái draft hoặc inactive).
         * DELETE /api/v1/mentor/me/packages/{packageId}
         */
        @DeleteMapping("/{packageId}")
        public ResponseEntity<ApiResponse<Void>> deletePackage(
                        @AuthenticationPrincipal CustomUserPrincipal principal,
                        @PathVariable Long packageId) {
                mentorService.deletePackage(principal.getUserId(), packageId);
                return ResponseEntity.ok(ApiResponse.<Void>build().withMessage("Package deleted successfully"));
        }
}
