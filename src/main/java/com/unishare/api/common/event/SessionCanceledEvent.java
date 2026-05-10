package com.unishare.api.common.event;

import java.util.UUID;

public record SessionCanceledEvent(
        UUID bookingId,
        UUID sessionId,
        UUID canceledBy,
        String cancelReason
) implements DomainEvent {
}
