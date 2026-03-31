package com.unishare.api.modules.document.service;

import com.unishare.api.common.dto.AppException;
import com.unishare.api.modules.document.dto.DocumentReviewRequest;
import com.unishare.api.modules.document.dto.DocumentReviewResponse;
import com.unishare.api.modules.document.entity.DocumentReview;
import com.unishare.api.modules.document.exception.DocumentErrorCode;
import com.unishare.api.modules.document.repository.DocumentReviewRepository;
import com.unishare.api.modules.document.service.DocumentService;
import com.unishare.api.modules.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DocumentReviewService {

    private final DocumentReviewRepository reviewRepository;
    private final DocumentService documentService;
    private final OrderService orderService;

    @Transactional
    public DocumentReviewResponse addReview(Long userId, Long documentId, DocumentReviewRequest request) {
        // Kiểm tra document tồn tại (có thể gọi getDocumentById hoặc isDocumentPublished)
        if (!documentService.isDocumentPublished(documentId)) {
            throw new AppException(DocumentErrorCode.DOCUMENT_NOT_FOUND, "Document is not available for review.");
        }

        // Kiểm tra xem user đã mua chưa
        if (!orderService.hasUserPurchasedDocument(userId, documentId)) {
            // Không được review nếu chưa mua (giả sử có mã lỗi chung hoặc dùng DOCUMENT_NOT_FOUND tạm)
            // Trong thực tế nên tạo ReviewErrorCode
            throw new AppException(DocumentErrorCode.DOCUMENT_NOT_FOUND, "You must purchase this document before reviewing.");
        }

        // Kiểm tra xem đã review chưa
        if (reviewRepository.existsByUserIdAndDocumentId(userId, documentId)) {
            throw new AppException(DocumentErrorCode.INVALID_DOCUMENT_STATUS, "You have already reviewed this document.");
        }

        DocumentReview review = new DocumentReview();
        review.setUserId(userId);
        review.setDocumentId(documentId);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review = reviewRepository.save(review);

        // Map feedback
        updateDocumentRating(documentId);

        DocumentReviewResponse response = new DocumentReviewResponse();
        response.setId(review.getId());
        response.setUserId(review.getUserId());
        response.setDocumentId(review.getDocumentId());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setCreatedAt(review.getCreatedAt());
        return response;
    }

    @Transactional(readOnly = true)
    public Page<DocumentReviewResponse> getDocumentReviews(Long documentId, Pageable pageable) {
        return reviewRepository.findByDocumentId(documentId, pageable)
                .map(review -> {
                    DocumentReviewResponse response = new DocumentReviewResponse();
                    response.setId(review.getId());
                    response.setUserId(review.getUserId());
                    response.setDocumentId(review.getDocumentId());
                    response.setRating(review.getRating());
                    response.setComment(review.getComment());
                    response.setCreatedAt(review.getCreatedAt());
                    return response;
                });
    }

    private void updateDocumentRating(Long documentId) {
        Double avgRating = reviewRepository.getAverageRating(documentId);
        // int count = (int) reviewRepository.countByDocumentId(documentId);
        
        documentService.updateDocumentRating(documentId, avgRating != null ? avgRating : 0.0);
    }
}
