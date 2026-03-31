package com.unishare.api.modules.order.service;

import com.unishare.api.modules.order.dto.OrderResponse;
import com.unishare.api.modules.order.dto.OrderRequest;

import java.util.List;

public interface OrderService {
    /**
     * Tạo đơn hàng mua document, trả về OrderResponse kèm VNPay paymentUrl
     */
    OrderResponse createDocumentOrder(Long buyerId, OrderRequest request);

    /**
     * Lấy danh sách đơn hàng theo buyer
     */
    List<OrderResponse> getMyOrders(Long buyerId);

    /**
     * Chi tiết đơn hàng
     */
    OrderResponse getOrderById(Long orderId, Long buyerId);

    /**
     * Kiểm tra buyer đã mua document chưa (dùng bởi Review module)
     */
    boolean hasUserPurchasedDocument(Long buyerId, Long documentId);
}
