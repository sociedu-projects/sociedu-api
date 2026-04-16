package com.unishare.api.modules.service.controller;

import com.unishare.api.common.dto.ApiResponse;
import com.unishare.api.config.OpenApiConfig;
import com.unishare.api.infrastructure.security.CustomUserPrincipal;
import com.unishare.api.modules.service.dto.request.ReviewReportRequest;
import com.unishare.api.modules.service.dto.response.ProgressReportResponse;
import com.unishare.api.modules.service.service.ProgressReportService;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/mentors/me/reports")
@RequiredArgsConstructor
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
@PreAuthorize("hasRole('MENTOR')")
@Tag(name = "Progress reports — Mentor")
public class MentorReportController {

    private final ProgressReportService reportService;

    @Operation(summary = "Báo cáo gán cho mentor")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProgressReportResponse>>> getAssignedReports(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
            
        List<ProgressReportResponse> reports = reportService.getMentorReports(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.<List<ProgressReportResponse>>build()
                .withData(reports)
                .withMessage("Success"));
    }

    @Operation(summary = "Phản hồi báo cáo")
    @PutMapping("/{id}/feedback")
    public ResponseEntity<ApiResponse<ProgressReportResponse>> reviewReport(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody ReviewReportRequest request) {
            
        ProgressReportResponse response = reportService.reviewReport(principal.getUserId(), id, request);
        return ResponseEntity.ok(ApiResponse.<ProgressReportResponse>build()
                .withData(response)
                .withMessage("Phản hồi báo cáo thành công"));
    }
}
