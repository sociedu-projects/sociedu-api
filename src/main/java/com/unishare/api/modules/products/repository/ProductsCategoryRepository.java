package com.unishare.api.modules.products.repository;

import com.unishare.api.modules.products.entity.ProductsCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductsCategoryRepository extends JpaRepository<ProductsCategory, Long> {
    List<ProductsCategory> findByParentId(Long parentId);
    List<ProductsCategory> findByParentIdIsNull();
}
