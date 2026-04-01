package com.unishare.api.modules.products.repository;

import com.unishare.api.modules.products.entity.ProductsAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductsAssetRepository extends JpaRepository<ProductsAsset, Long> {
    List<ProductsAsset> findByDocumentId(Long documentId);
}
