package com.unishare.api.modules.products.repository;

import com.unishare.api.modules.products.entity.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Products, Long> {
    List<Products> findBySellerId(Long sellerId);
    List<Products> findByStatus(String status);
}
