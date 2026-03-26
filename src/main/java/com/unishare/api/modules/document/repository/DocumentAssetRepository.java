package com.unishare.api.modules.document.repository;

import com.unishare.api.modules.document.entity.DocumentAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentAssetRepository extends JpaRepository<DocumentAsset, Long> {
    List<DocumentAsset> findByDocumentId(Long documentId);
}
