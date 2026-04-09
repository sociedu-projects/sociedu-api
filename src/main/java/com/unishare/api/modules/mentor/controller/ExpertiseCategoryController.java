package com.unishare.api.modules.mentor.controller;

import com.unishare.api.common.dto.ApiResponse;
import com.unishare.api.modules.mentor.dto.MentorDto.*;
import com.unishare.api.modules.mentor.service.MentorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public API for expertise/specialization categories.
 * Used in search filters, profile tagging, and admin management.
 */
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class ExpertiseCategoryController {

    private final MentorService mentorService;

    /**
     * Lấy danh sách danh mục chuyên môn (hierarchical tree).
     * Danh mục dùng thống nhất giữa search, profile, admin.
     * GET /api/v1/categories/expertise
     */
    @GetMapping("/expertise")
    public ResponseEntity<ApiResponse<List<ExpertiseCategoryResponse>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.<List<ExpertiseCategoryResponse>>build()
                .withData(mentorService.getAllCategories()));
    }
}
