package com.unishare.api.modules.products.service.impl;

import com.unishare.api.common.dto.AppException;
import com.unishare.api.modules.products.dto.DocumentRequest;
import com.unishare.api.modules.products.dto.DocumentResponse;
import com.unishare.api.modules.products.entity.Products;
import com.unishare.api.modules.products.entity.WishlistItem;
import com.unishare.api.modules.products.exception.ProductsErrorCode;
import com.unishare.api.modules.products.mapper.ProductsMapper;
import com.unishare.api.modules.products.repository.DocumentRepository;
import com.unishare.api.modules.products.repository.WishlistItemRepository;
import com.unishare.api.modules.products.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final WishlistItemRepository wishlistRepository;
    private final ProductsMapper productsMapper;

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getAllDocuments() {
        return documentRepository.findAll().stream()
                .map(productsMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getSellerDocuments(Long sellerId) {
        return documentRepository.findBySellerId(sellerId).stream()
                .map(productsMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponse getDocumentById(Long id) {
        Products products = documentRepository.findById(id)
                .orElseThrow(() -> new AppException(ProductsErrorCode.DOCUMENT_NOT_FOUND));
        return productsMapper.toResponse(products);
    }

    @Override
    @Transactional
    public DocumentResponse createDocument(Long sellerId, DocumentRequest request) {
        Products products = productsMapper.toEntity(request, sellerId);
        Products saved = documentRepository.save(products);
        return productsMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public DocumentResponse updateDocument(Long sellerId, Long documentId, DocumentRequest request) {
        Products products = documentRepository.findById(documentId)
                .filter(d -> d.getSellerId().equals(sellerId))
                .orElseThrow(() -> new AppException(ProductsErrorCode.DOCUMENT_NOT_FOUND));
        
        productsMapper.updateEntity(products, request);
        Products saved = documentRepository.save(products);
        return productsMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteDocument(Long sellerId, Long documentId) {
        Products products = documentRepository.findById(documentId)
                .filter(d -> d.getSellerId().equals(sellerId))
                .orElseThrow(() -> new AppException(ProductsErrorCode.DOCUMENT_NOT_FOUND));
        documentRepository.delete(products);
    }

    // Wishlist
    @Override
    @Transactional
    public void addToWishlist(Long userId, Long documentId) {
        if (!documentRepository.existsById(documentId)) {
            throw new AppException(ProductsErrorCode.DOCUMENT_NOT_FOUND);
        }
        if (!wishlistRepository.existsByUserIdAndDocumentId(userId, documentId)) {
            WishlistItem item = new WishlistItem();
            item.setUserId(userId);
            item.setDocumentId(documentId);
            wishlistRepository.save(item);
        }
    }

    @Override
    @Transactional
    public void removeFromWishlist(Long userId, Long documentId) {
        wishlistRepository.deleteByUserIdAndDocumentId(userId, documentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getUserWishlist(Long userId) {
        return wishlistRepository.findByUserId(userId).stream()
                .map(item -> documentRepository.findById(item.getDocumentId()).orElse(null))
                .filter(doc -> doc != null)
                .map(productsMapper::toResponse)
                .collect(Collectors.toList());
    }

    // Support methods for external modules
    @Override
    @Transactional(readOnly = true)
    public boolean isDocumentPublished(Long documentId) {
        return documentRepository.findById(documentId)
                .map(d -> "published".equals(d.getStatus()))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public java.math.BigDecimal getDocumentPrice(Long documentId) {
        return documentRepository.findById(documentId)
                .map(Products::getPrice)
                .orElse(java.math.BigDecimal.ZERO);
    }

    @Override
    @Transactional(readOnly = true)
    public String getDocumentTitle(Long documentId) {
        return documentRepository.findById(documentId)
                .map(Products::getTitle)
                .orElse("");
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDocumentOwnedBySeller(Long documentId, Long sellerId) {
        return documentRepository.findById(documentId)
                .map(d -> d.getSellerId().equals(sellerId))
                .orElse(false);
    }

    @Override
    @Transactional
    public void updateDocumentRating(Long documentId, double newAverage) {
        documentRepository.findById(documentId).ifPresent(d -> {
            d.setRatingAvg(newAverage);
            // Có thể bổ sung increment sales_count/review_count nếu sau này CSDL map. Tạm thời db chỉ có rating_avg.
            documentRepository.save(d);
        });
    }
}
