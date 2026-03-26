package com.unishare.api.modules.document.controller;

import com.unishare.api.common.dto.ApiResponse;
import com.unishare.api.modules.document.dto.DocumentCategoryRequest;
import com.unishare.api.modules.document.dto.DocumentCategoryResponse;
import com.unishare.api.modules.document.service.DocumentCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/document-categories")
@RequiredArgsConstructor
public class DocumentCategoryController {

    private final DocumentCategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DocumentCategoryResponse>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.<List<DocumentCategoryResponse>>build()
                .withData(categoryService.getAllCategories()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentCategoryResponse>> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<DocumentCategoryResponse>build()
                .withData(categoryService.getCategoryById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DocumentCategoryResponse>> createCategory(
            @Valid @RequestBody DocumentCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<DocumentCategoryResponse>build()
                .withHttpStatus(HttpStatus.CREATED)
                .withData(categoryService.createCategory(request))
                .withMessage("Category created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentCategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody DocumentCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.<DocumentCategoryResponse>build()
                .withData(categoryService.updateCategory(id, request))
                .withMessage("Category updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.<Void>build().withMessage("Category deleted successfully"));
    }
}
