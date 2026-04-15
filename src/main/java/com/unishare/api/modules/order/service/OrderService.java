package com.unishare.api.modules.order.service;

import com.unishare.api.modules.order.dto.CheckoutRequest;
import com.unishare.api.modules.order.dto.OrderResponse;
import com.unishare.api.modules.order.dto.OrderSnapshot;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    List<OrderResponse> getMyOrders(UUID buyerId);

    OrderResponse getOrderById(UUID orderId, UUID buyerId);

    /** Tạo đơn + URL thanh toán VNPay. */
    OrderResponse checkout(UUID buyerId, CheckoutRequest request, String clientIp);

    OrderSnapshot getOrderSnapshot(UUID orderId);

    void applyPaymentResult(UUID orderId, boolean success);
}
