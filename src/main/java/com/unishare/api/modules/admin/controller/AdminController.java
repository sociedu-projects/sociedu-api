package com.unishare.api.modules.admin.controller;

import com.unishare.api.common.dto.ApiResponse;
import com.unishare.api.modules.admin.dto.AdminDto.AdminStatsResponse;
import com.unishare.api.modules.admin.service.AdminService;
import com.unishare.api.modules.mentor.dto.MentorDto.MentorProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.<AdminStatsResponse>build()
                .withData(adminService.getSystemStats()));
    }

    @GetMapping("/mentor-requests")
    public ResponseEntity<ApiResponse<List<MentorProfileResponse>>> getMentorRequests() {
        return ResponseEntity.ok(ApiResponse.<List<MentorProfileResponse>>build()
                .withData(adminService.getPendingMentorRequests()));
    }

    @PostMapping("/mentor-requests/{id}/approve")
    public ResponseEntity<ApiResponse<MentorProfileResponse>> approveMentorRequest(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<MentorProfileResponse>build()
                .withData(adminService.approveMentorRequest(id))
                .withMessage("Mentor approved successfully"));
    }
}

