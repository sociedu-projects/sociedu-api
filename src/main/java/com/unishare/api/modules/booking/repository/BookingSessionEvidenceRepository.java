package com.unishare.api.modules.booking.repository;

import com.unishare.api.modules.booking.entity.BookingSessionEvidence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BookingSessionEvidenceRepository extends JpaRepository<BookingSessionEvidence, UUID> {

    List<BookingSessionEvidence> findByBookingSessionId(UUID bookingSessionId);
}
