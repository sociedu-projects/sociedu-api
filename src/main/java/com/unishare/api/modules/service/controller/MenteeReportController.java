package com.unishare.api.modules.service.controller;

import com.unishare.api.common.dto.ApiResponse;
import com.unishare.api.config.OpenApiConfig;
import com.unishare.api.infrastructure.security.CustomUserPrincipal;
import com.unishare.api.modules.service.dto.request.CreateReportRequest;
import com.unishare.api.modules.service.dto.response.ProgressReportResponse;
import com.unishare.api.modules.service.service.ProgressReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/mentee/reports")
@RequiredArgsConstructor
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
@Tag(name = "Progress reports â€” Mentee")
public class MenteeReportController {

    private final ProgressReportService reportService;

    @Operation(summary = "Nộp báo cáo tiến độ")
    @PreAuthorize("hasAuthority(T(com.unishare.api.common.constants.Capabilities).CREATE_REPORT)")
    @PostMapping
    public ResponseEntity<ApiResponse<ProgressReportResponse>> submitReport(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody CreateReportRequest request) {
        ProgressReportResponse response = reportService.createReport(principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.<ProgressReportResponse>build()
                .withData(response)
                .withMessage("Nộp báo cáo thành công"));
    }

    @Operation(summary = "Báo cáo của tôi (mentee)")
    @PreAuthorize("hasAuthority(T(com.unishare.api.common.constants.Capabilities).VIEW_OWN_REPORT)")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProgressReportResponse>>> getMyReports(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            Pageable pageable) {
        Page<ProgressReportResponse> reports = reportService.getMenteeReports(principal.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.<Page<ProgressReportResponse>>build()
                .withData(reports)
                .withMessage("Success"));
    }
}
