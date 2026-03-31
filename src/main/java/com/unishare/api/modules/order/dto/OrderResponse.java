package com.unishare.api.modules.order.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class OrderResponse {
    private Long id;
    private Long buyerId;
    private String type;
    private String status;
    private BigDecimal totalAmount;
    private Instant createdAt;
    private List<OrderItemResponse> items;

    // VNPay payment URL - có giá trị khi order mới tạo (status=pending)
    private String paymentUrl;
}
