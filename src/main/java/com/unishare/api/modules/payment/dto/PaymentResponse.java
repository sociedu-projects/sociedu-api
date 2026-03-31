package com.unishare.api.modules.payment.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private String provider;
    private String transactionRef;
    private String status;
    private Instant createdAt;
    // VNPay payment URL (if pending)
    private String paymentUrl;
}
