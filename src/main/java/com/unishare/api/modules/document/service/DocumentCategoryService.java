package com.unishare.api.modules.document.service;

import com.unishare.api.modules.document.dto.DocumentCategoryRequest;
import com.unishare.api.modules.document.dto.DocumentCategoryResponse;

import java.util.List;

public interface DocumentCategoryService {
    List<DocumentCategoryResponse> getAllCategories();
    DocumentCategoryResponse getCategoryById(Long id);
    DocumentCategoryResponse createCategory(DocumentCategoryRequest request);
    DocumentCategoryResponse updateCategory(Long id, DocumentCategoryRequest request);
    void deleteCategory(Long id);
}
