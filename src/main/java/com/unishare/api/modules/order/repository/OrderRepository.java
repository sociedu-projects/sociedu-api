package com.unishare.api.modules.order.repository;

import com.unishare.api.modules.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByBuyerId(Long buyerId);

    @Query("""
        SELECT COUNT(o) > 0 FROM Order o
        JOIN OrderItem oi ON oi.orderId = o.id
        WHERE o.buyerId = :buyerId
          AND oi.itemType = 'products'
          AND oi.itemId = :documentId
          AND o.status = 'completed'
    """)
    boolean hasBuyerPurchasedDocument(@Param("buyerId") Long buyerId, @Param("documentId") Long documentId);
}
