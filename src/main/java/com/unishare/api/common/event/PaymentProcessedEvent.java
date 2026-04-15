package com.unishare.api.common.event;

import java.util.UUID;

/**
 * Cổng thanh toán đã xử lý xong callback và lưu {@code payment_transactions} — listener sẽ cập nhật đơn ({@code orders}).
 */
public record PaymentProcessedEvent(
        UUID orderId,
        boolean success,
        String provider,
        String providerTransactionId,
        UUID paymentTransactionId
)
        implements DomainEvent {}
