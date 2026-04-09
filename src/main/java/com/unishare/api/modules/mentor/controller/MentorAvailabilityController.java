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
 * Mentor self-management endpoints for availability slots.
 * Requires MENTOR role.
 */
@RestController
@RequestMapping("/api/v1/mentor/me/availability")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MENTOR')")
public class MentorAvailabilityController {

        private final MentorService mentorService;

        /**
         * Tạo slot lịch rảnh mới.
         * Hỗ trợ recurrence (none/daily/weekly) — tự sinh slot cho 4 tuần tới.
         * Slot chỉ được tạo khi mentor ở trạng thái verified.
         * POST /api/v1/mentor/me/availability
         */
        @PostMapping
        public ResponseEntity<ApiResponse<List<AvailabilitySlotResponse>>> createSlot(
                        @AuthenticationPrincipal CustomUserPrincipal principal,
                        @Valid @RequestBody AvailabilitySlotRequest request) {
                List<AvailabilitySlotResponse> response = mentorService.createSlot(
                                principal.getUserId(), request);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.<List<AvailabilitySlotResponse>>build()
                                                .withData(response)
                                                .withMessage("Availability slot(s) created successfully"));
        }

        /**
         * Cập nhật / chặn / huỷ slot.
         * Không được sửa slot đã có booking (status = booked).
         * Có thể check xung đột slot.
         * PATCH /api/v1/mentor/me/availability/{slotId}
         */
        @PatchMapping("/{slotId}")
        public ResponseEntity<ApiResponse<AvailabilitySlotResponse>> updateSlot(
                        @AuthenticationPrincipal CustomUserPrincipal principal,
                        @PathVariable Long slotId,
                        @Valid @RequestBody AvailabilitySlotUpdateRequest request) {
                return ResponseEntity.ok(ApiResponse.<AvailabilitySlotResponse>build()
                                .withData(mentorService.updateSlot(principal.getUserId(), slotId, request)));
        }
}
