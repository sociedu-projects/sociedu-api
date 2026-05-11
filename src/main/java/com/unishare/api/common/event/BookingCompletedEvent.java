package com.unishare.api.common.event;

import java.util.UUID;

public record BookingCompletedEvent(UUID bookingId, UUID mentorId, UUID buyerId, UUID orderId) implements DomainEvent {
    public String name() {
        return "BookingCompletedEvent";
    }
}
