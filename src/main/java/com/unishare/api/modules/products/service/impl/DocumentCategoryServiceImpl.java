package com.unishare.api.modules.products.service.impl;

import com.unishare.api.common.dto.AppException;
import com.unishare.api.modules.products.dto.DocumentCategoryRequest;
import com.unishare.api.modules.products.dto.DocumentCategoryResponse;
import com.unishare.api.modules.products.entity.ProductsCategory;
import com.unishare.api.modules.products.exception.ProductsErrorCode;
import com.unishare.api.modules.products.mapper.ProductsMapper;
import com.unishare.api.modules.products.repository.ProductsCategoryRepository;
import com.unishare.api.modules.products.service.DocumentCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentCategoryServiceImpl implements DocumentCategoryService {

    private final ProductsCategoryRepository categoryRepository;
    private final ProductsMapper productsMapper;

    @Override
    @Transactional(readOnly = true)
    public List<DocumentCategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(productsMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentCategoryResponse getCategoryById(Long id) {
        ProductsCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ProductsErrorCode.CATEGORY_NOT_FOUND));
        return productsMapper.toResponse(category);
    }

    @Override
    @Transactional
    public DocumentCategoryResponse createCategory(DocumentCategoryRequest request) {
        ProductsCategory category = productsMapper.toEntity(request);
        ProductsCategory saved = categoryRepository.save(category);
        return productsMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public DocumentCategoryResponse updateCategory(Long id, DocumentCategoryRequest request) {
        ProductsCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ProductsErrorCode.CATEGORY_NOT_FOUND));
        productsMapper.updateEntity(category, request);
        ProductsCategory saved = categoryRepository.save(category);
        return productsMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new AppException(ProductsErrorCode.CATEGORY_NOT_FOUND);
        }
        categoryRepository.deleteById(id);
    }
}
