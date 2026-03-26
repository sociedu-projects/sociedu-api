package com.unishare.api.modules.document.repository;

import com.unishare.api.modules.document.entity.WishlistItem;
import com.unishare.api.modules.document.entity.WishlistItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, WishlistItemId> {
    List<WishlistItem> findByUserId(Long userId);
    boolean existsByUserIdAndDocumentId(Long userId, Long documentId);
    void deleteByUserIdAndDocumentId(Long userId, Long documentId);
}
