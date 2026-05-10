package com.unishare.api.common.event;

import java.util.UUID;

public record BookingCanceledEvent(
        UUID bookingId,
        UUID orderId,
        UUID canceledBy,
        String cancelReason
) implements DomainEvent {
}
