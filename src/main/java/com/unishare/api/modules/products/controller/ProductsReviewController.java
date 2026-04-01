package com.unishare.api.modules.products.controller;

import com.unishare.api.common.dto.ApiResponse;
import com.unishare.api.infrastructure.security.CustomUserPrincipal;
import com.unishare.api.modules.products.dto.DocumentReviewRequest;
import com.unishare.api.modules.products.dto.DocumentReviewResponse;
import com.unishare.api.modules.products.service.DocumentReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/documents/{documentId}/reviews")
@RequiredArgsConstructor
public class ProductsReviewController {

    private final DocumentReviewService reviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<DocumentReviewResponse>> addReview(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long documentId,
            @Valid @RequestBody DocumentReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<DocumentReviewResponse>build()
                        .withHttpStatus(HttpStatus.CREATED)
                        .withData(reviewService.addReview(principal.getUserId(), documentId, request))
                        .withMessage("Review added successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<DocumentReviewResponse>>> getReviews(
            @PathVariable Long documentId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.<Page<DocumentReviewResponse>>build()
                .withData(reviewService.getDocumentReviews(documentId, pageable)));
    }
}
