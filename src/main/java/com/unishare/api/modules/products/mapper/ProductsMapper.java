package com.unishare.api.modules.products.mapper;

import com.unishare.api.modules.products.dto.*;
import com.unishare.api.modules.products.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductsMapper {

    // --- DocumentCategory ---
    public DocumentCategoryResponse toResponse(ProductsCategory entity) {
        if (entity == null) return null;
        DocumentCategoryResponse response = new DocumentCategoryResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setParentId(entity.getParentId());
        return response;
    }

    public ProductsCategory toEntity(DocumentCategoryRequest request) {
        if (request == null) return null;
        ProductsCategory entity = new ProductsCategory();
        entity.setName(request.getName());
        entity.setParentId(request.getParentId());
        return entity;
    }

    public void updateEntity(ProductsCategory entity, DocumentCategoryRequest request) {
        if (request.getName() != null) entity.setName(request.getName());
        if (request.getParentId() != null) entity.setParentId(request.getParentId());
    }

    // --- DocumentAsset ---
    public DocumentAssetResponse toResponse(ProductsAsset entity) {
        if (entity == null) return null;
        DocumentAssetResponse response = new DocumentAssetResponse();
        response.setId(entity.getId());
        response.setFileType(entity.getFileType());
        response.setFileFormat(entity.getFileFormat());
        response.setFileUrl(entity.getFileUrl());
        response.setFileSize(entity.getFileSize());
        response.setSortOrder(entity.getSortOrder());
        return response;
    }

    public ProductsAsset toEntity(DocumentAssetRequest request, Products products) {
        if (request == null) return null;
        ProductsAsset entity = new ProductsAsset();
        entity.setProducts(products);
        entity.setFileType(request.getFileType());
        entity.setFileFormat(request.getFileFormat());
        entity.setFileUrl(request.getFileUrl());
        entity.setFileSize(request.getFileSize());
        if (request.getSortOrder() != null) entity.setSortOrder(request.getSortOrder());
        return entity;
    }

    // --- Document ---
    public DocumentResponse toResponse(Products entity) {
        if (entity == null) return null;
        DocumentResponse response = new DocumentResponse();
        response.setId(entity.getId());
        response.setSellerId(entity.getSellerId());
        response.setTitle(entity.getTitle());
        response.setDescription(entity.getDescription());
        response.setSubject(entity.getSubject());
        response.setUniversity(entity.getUniversity());
        response.setMajor(entity.getMajor());
        response.setDocType(entity.getDocType());
        response.setPrice(entity.getPrice());
        response.setStatus(entity.getStatus());
        response.setRatingAvg(entity.getRatingAvg());
        response.setSalesCount(entity.getSalesCount());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getAssets() != null) {
            response.setAssets(entity.getAssets().stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList()));
        }

        return response;
    }

    public Products toEntity(DocumentRequest request, Long sellerId) {
        if (request == null) return null;
        Products entity = new Products();
        entity.setSellerId(sellerId);
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setSubject(request.getSubject());
        entity.setUniversity(request.getUniversity());
        entity.setMajor(request.getMajor());
        entity.setDocType(request.getDocType());
        entity.setPrice(request.getPrice());
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }

        if (request.getAssets() != null) {
            List<ProductsAsset> assets = request.getAssets().stream()
                    .map(assetRequest -> toEntity(assetRequest, entity))
                    .collect(Collectors.toList());
            entity.setAssets(assets);
        }

        return entity;
    }

    public void updateEntity(Products entity, DocumentRequest request) {
        if (request.getTitle() != null) entity.setTitle(request.getTitle());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());
        if (request.getSubject() != null) entity.setSubject(request.getSubject());
        if (request.getUniversity() != null) entity.setUniversity(request.getUniversity());
        if (request.getMajor() != null) entity.setMajor(request.getMajor());
        if (request.getDocType() != null) entity.setDocType(request.getDocType());
        if (request.getPrice() != null) entity.setPrice(request.getPrice());
        if (request.getStatus() != null) entity.setStatus(request.getStatus());

        if (request.getAssets() != null) {
            entity.getAssets().clear();
            List<ProductsAsset> newAssets = request.getAssets().stream()
                    .map(assetRequest -> toEntity(assetRequest, entity))
                    .collect(Collectors.toList());
            entity.getAssets().addAll(newAssets);
        }
    }
}
