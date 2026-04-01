package com.unishare.api.modules.order.service;

import com.unishare.api.modules.order.dto.OrderResponse;

import java.util.List;

public interface OrderService {
    /**
     * Lấy danh sách đơn hàng theo buyer
     */
    List<OrderResponse> getMyOrders(Long buyerId);

    /**
     * Chi tiết đơn hàng
     */
    OrderResponse getOrderById(Long orderId, Long buyerId);
}
