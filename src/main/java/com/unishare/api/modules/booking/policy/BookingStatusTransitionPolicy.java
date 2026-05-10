package com.unishare.api.modules.booking.policy;

import com.unishare.api.common.constants.BookingStatuses;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class BookingStatusTransitionPolicy {

    private static final Map<String, Set<String>> ALLOWED_TRANSITIONS = Map.of(
            BookingStatuses.PENDING, Set.of(BookingStatuses.SCHEDULED, BookingStatuses.IN_PROGRESS, BookingStatuses.CANCELED),
            BookingStatuses.SCHEDULED, Set.of(BookingStatuses.IN_PROGRESS, BookingStatuses.CANCELED),
            BookingStatuses.IN_PROGRESS, Set.of(BookingStatuses.COMPLETED, BookingStatuses.CANCELED, "disputed"), // "disputed" can be added to constants later if needed
            BookingStatuses.COMPLETED, Collections.emptySet(),
            BookingStatuses.CANCELED, Set.of(BookingStatuses.REFUNDED),
            BookingStatuses.REFUNDED, Collections.emptySet(),
            "disputed", Set.of(BookingStatuses.REFUNDED, BookingStatuses.COMPLETED, BookingStatuses.CANCELED)
    );

    public static void validateTransition(String fromStatus, String toStatus) {
        if (fromStatus == null || toStatus == null) {
            throw new IllegalArgumentException("Booking status cannot be null");
        }
        if (fromStatus.equals(toStatus)) {
            return; // No-op, allowed by idempotency
        }
        Set<String> allowed = ALLOWED_TRANSITIONS.getOrDefault(fromStatus, Collections.emptySet());
        if (!allowed.contains(toStatus)) {
            throw new IllegalStateException("Invalid booking status transition from " + fromStatus + " to " + toStatus);
        }
    }
}
