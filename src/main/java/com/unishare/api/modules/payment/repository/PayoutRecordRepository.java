package com.unishare.api.modules.payment.repository;

import com.unishare.api.modules.payment.entity.PayoutRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayoutRecordRepository extends JpaRepository<PayoutRecord, UUID> {
    Optional<PayoutRecord> findByBookingId(UUID bookingId);
}
