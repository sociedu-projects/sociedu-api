package com.unishare.api.modules.document.repository;

import com.unishare.api.modules.document.entity.DocumentReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentReviewRepository extends JpaRepository<DocumentReview, Long> {

    Page<DocumentReview> findByDocumentId(Long documentId, Pageable pageable);

    boolean existsByUserIdAndDocumentId(Long userId, Long documentId);

    @Query("SELECT AVG(r.rating) FROM DocumentReview r WHERE r.documentId = :documentId")
    Double getAverageRating(@Param("documentId") Long documentId);

    long countByDocumentId(Long documentId);
}
