package com.unishare.api.modules.document.service.impl;

import com.unishare.api.common.dto.AppException;
import com.unishare.api.modules.document.dto.DocumentRequest;
import com.unishare.api.modules.document.dto.DocumentResponse;
import com.unishare.api.modules.document.entity.Document;
import com.unishare.api.modules.document.entity.WishlistItem;
import com.unishare.api.modules.document.exception.DocumentErrorCode;
import com.unishare.api.modules.document.mapper.DocumentMapper;
import com.unishare.api.modules.document.repository.DocumentRepository;
import com.unishare.api.modules.document.repository.WishlistItemRepository;
import com.unishare.api.modules.document.service.DocumentService;
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
    private final DocumentMapper documentMapper;

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getAllDocuments() {
        return documentRepository.findAll().stream()
                .map(documentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getSellerDocuments(Long sellerId) {
        return documentRepository.findBySellerId(sellerId).stream()
                .map(documentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponse getDocumentById(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new AppException(DocumentErrorCode.DOCUMENT_NOT_FOUND));
        return documentMapper.toResponse(document);
    }

    @Override
    @Transactional
    public DocumentResponse createDocument(Long sellerId, DocumentRequest request) {
        Document document = documentMapper.toEntity(request, sellerId);
        Document saved = documentRepository.save(document);
        return documentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public DocumentResponse updateDocument(Long sellerId, Long documentId, DocumentRequest request) {
        Document document = documentRepository.findById(documentId)
                .filter(d -> d.getSellerId().equals(sellerId))
                .orElseThrow(() -> new AppException(DocumentErrorCode.DOCUMENT_NOT_FOUND));
        
        documentMapper.updateEntity(document, request);
        Document saved = documentRepository.save(document);
        return documentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteDocument(Long sellerId, Long documentId) {
        Document document = documentRepository.findById(documentId)
                .filter(d -> d.getSellerId().equals(sellerId))
                .orElseThrow(() -> new AppException(DocumentErrorCode.DOCUMENT_NOT_FOUND));
        documentRepository.delete(document);
    }

    // Wishlist
    @Override
    @Transactional
    public void addToWishlist(Long userId, Long documentId) {
        if (!documentRepository.existsById(documentId)) {
            throw new AppException(DocumentErrorCode.DOCUMENT_NOT_FOUND);
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
                .map(documentMapper::toResponse)
                .collect(Collectors.toList());
    }
}
