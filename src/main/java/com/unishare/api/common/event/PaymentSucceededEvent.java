package com.unishare.api.common.event;

import java.util.UUID;

/**
 * Phát sau khi đơn đã chuyển sang paid (listener xử lý {@link PaymentProcessedEvent}).
 * Dùng cho audit / webhook — có mã giao dịch phía cổng.
 */
public record PaymentSucceededEvent(
        UUID orderId,
        UUID buyerId,
        String provider,
        String providerTransactionId,
        UUID paymentTransactionId
)
        implements DomainEvent {}
