package com.unishare.api.common.event;

import java.util.UUID;

/**
 * Booking đã được tạo sau khi order paid (sessions đã seed).
 */
public record BookingCreatedEvent(UUID bookingId, UUID orderId, UUID buyerId, UUID mentorId)
        implements DomainEvent {}
