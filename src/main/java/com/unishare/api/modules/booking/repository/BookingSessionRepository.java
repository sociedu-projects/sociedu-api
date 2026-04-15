package com.unishare.api.modules.booking.repository;

import com.unishare.api.modules.booking.entity.BookingSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BookingSessionRepository extends JpaRepository<BookingSession, UUID> {

    List<BookingSession> findByBookingIdOrderByScheduledAtAsc(UUID bookingId);
}
