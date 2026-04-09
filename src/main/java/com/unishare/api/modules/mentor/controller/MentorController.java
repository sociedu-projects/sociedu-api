package com.unishare.api.modules.mentor.controller;

import com.unishare.api.common.dto.ApiResponse;
import com.unishare.api.infrastructure.security.CustomUserPrincipal;
import com.unishare.api.modules.mentor.dto.MentorDto.*;
import com.unishare.api.modules.mentor.service.MentorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public mentor endpoints — accessible without authentication.
 * Handles mentor listing, profile detail, packages, and availability.
 */
@RestController
@RequestMapping("/api/v1/mentors")
@RequiredArgsConstructor
public class MentorController {

        private final MentorService mentorService;

        // ===================== PUBLIC ENDPOINTS =====================

        /**
         * Tìm kiếm và lọc danh sách mentor công khai.
         * Hỗ trợ search, filter, sort, pagination.
         * GET
         * /api/v1/mentors?keyword=...&expertise=...&minPrice=...&maxPrice=...&sortBy=...&page=0&size=20
         */
        @GetMapping
        public ResponseEntity<ApiResponse<Page<MentorListResponse>>> searchMentors(
                        @ModelAttribute MentorSearchRequest request,
                        @PageableDefault(size = 20) Pageable pageable) {
                return ResponseEntity.ok(ApiResponse.<Page<MentorListResponse>>build()
                                .withData(mentorService.searchMentors(request, pageable)));
        }

        /**
         * Lấy thông tin hồ sơ chi tiết của một mentor.
         * Bao gồm: profile, stats, packages (active), availability preview.
         * GET /api/v1/mentors/{mentorId}
         */
        @GetMapping("/{mentorId}")
        public ResponseEntity<ApiResponse<MentorProfileResponse>> getMentorProfile(
                        @PathVariable Long mentorId) {
                return ResponseEntity.ok(ApiResponse.<MentorProfileResponse>build()
                                .withData(mentorService.getMentorProfile(mentorId)));
        }

        /**
         * Lấy danh sách package active (công khai) của mentor.
         * GET /api/v1/mentors/{mentorId}/packages
         */
        @GetMapping("/{mentorId}/packages")
        public ResponseEntity<ApiResponse<List<ServicePackageResponse>>> getMentorPackages(
                        @PathVariable Long mentorId) {
                return ResponseEntity.ok(ApiResponse.<List<ServicePackageResponse>>build()
                                .withData(mentorService.getPublicMentorPackages(mentorId)));
        }

        /**
         * Xem lịch rảnh available của mentor (public).
         * Chỉ hiện slot available, không hiện blocked/booked.
         * GET /api/v1/mentors/{mentorId}/availability
         */
        @GetMapping("/{mentorId}/availability")
        public ResponseEntity<ApiResponse<List<AvailabilitySlotResponse>>> getMentorAvailability(
                        @PathVariable Long mentorId) {
                return ResponseEntity.ok(ApiResponse.<List<AvailabilitySlotResponse>>build()
                                .withData(mentorService.getMentorAvailability(mentorId)));
        }

        // ===================== MENTOR SELF-MANAGEMENT =====================

        /**
         * Cập nhật (hoặc tạo mới) hồ sơ mentor của chính người dùng đang đăng nhập.
         * PUT /api/v1/mentors/me
         */
        @PutMapping("/me")
        public ResponseEntity<ApiResponse<MentorProfileResponse>> updateMyProfile(
                        @AuthenticationPrincipal CustomUserPrincipal principal,
                        @Valid @RequestBody MentorProfileRequest request) {
                return ResponseEntity.ok(ApiResponse.<MentorProfileResponse>build()
                                .withData(mentorService.createOrUpdateProfile(principal.getUserId(), request)));
        }
}
