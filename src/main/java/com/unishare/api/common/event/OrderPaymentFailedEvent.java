package com.unishare.api.common.event;

import java.util.UUID;

/**
 * Thanh toán thất bại (callback VNPay hoặc provider khác).
 */
public record OrderPaymentFailedEvent(UUID orderId, UUID buyerId) implements DomainEvent {}
