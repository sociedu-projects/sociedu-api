package com.unishare.api.modules.document.service.impl;

import com.unishare.api.common.dto.AppException;
import com.unishare.api.modules.document.dto.DocumentCategoryRequest;
import com.unishare.api.modules.document.dto.DocumentCategoryResponse;
import com.unishare.api.modules.document.entity.DocumentCategory;
import com.unishare.api.modules.document.exception.DocumentErrorCode;
import com.unishare.api.modules.document.mapper.DocumentMapper;
import com.unishare.api.modules.document.repository.DocumentCategoryRepository;
import com.unishare.api.modules.document.service.DocumentCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentCategoryServiceImpl implements DocumentCategoryService {

    private final DocumentCategoryRepository categoryRepository;
    private final DocumentMapper documentMapper;

    @Override
    @Transactional(readOnly = true)
    public List<DocumentCategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(documentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentCategoryResponse getCategoryById(Long id) {
        DocumentCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(DocumentErrorCode.CATEGORY_NOT_FOUND));
        return documentMapper.toResponse(category);
    }

    @Override
    @Transactional
    public DocumentCategoryResponse createCategory(DocumentCategoryRequest request) {
        DocumentCategory category = documentMapper.toEntity(request);
        DocumentCategory saved = categoryRepository.save(category);
        return documentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public DocumentCategoryResponse updateCategory(Long id, DocumentCategoryRequest request) {
        DocumentCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(DocumentErrorCode.CATEGORY_NOT_FOUND));
        documentMapper.updateEntity(category, request);
        DocumentCategory saved = categoryRepository.save(category);
        return documentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new AppException(DocumentErrorCode.CATEGORY_NOT_FOUND);
        }
        categoryRepository.deleteById(id);
    }
}
