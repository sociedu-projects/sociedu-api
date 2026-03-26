package com.unishare.api.modules.document.repository;

import com.unishare.api.modules.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findBySellerId(Long sellerId);
    List<Document> findByStatus(String status);
}
