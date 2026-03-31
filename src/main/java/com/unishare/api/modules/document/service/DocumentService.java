package com.unishare.api.modules.document.service;

import com.unishare.api.modules.document.dto.DocumentRequest;
import com.unishare.api.modules.document.dto.DocumentResponse;

import java.util.List;

public interface DocumentService {
    List<DocumentResponse> getAllDocuments();
    List<DocumentResponse> getSellerDocuments(Long sellerId);
    DocumentResponse getDocumentById(Long id);
    DocumentResponse createDocument(Long sellerId, DocumentRequest request);
    DocumentResponse updateDocument(Long sellerId, Long documentId, DocumentRequest request);
    void deleteDocument(Long sellerId, Long documentId);
    
    // Support methods for external modules
    boolean isDocumentPublished(Long documentId);
    java.math.BigDecimal getDocumentPrice(Long documentId);
    String getDocumentTitle(Long documentId);
    boolean isDocumentOwnedBySeller(Long documentId, Long sellerId);
    void updateDocumentRating(Long documentId, double newAverage);
    
    // Wishlist
    void addToWishlist(Long userId, Long documentId);
    void removeFromWishlist(Long userId, Long documentId);
    List<DocumentResponse> getUserWishlist(Long userId);
}

