package com.unishare.api.modules.payment.listener;

import com.unishare.api.common.event.BookingCompletedEvent;
import com.unishare.api.modules.order.dto.OrderSnapshot;
import com.unishare.api.modules.order.service.OrderService;
import com.unishare.api.modules.payment.entity.PayoutRecord;
import com.unishare.api.modules.payment.repository.PayoutRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayoutRecordListener {

    private final PayoutRecordRepository payoutRecordRepository;
    private final OrderService orderService;

    /**
     * Lắng nghe sự kiện BookingCompletedEvent CHỈ SAU KHI transaction gốc đã commit thành công.
     * Mở một transaction mới (REQUIRES_NEW) để insert PayoutRecord.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleBookingCompletedEvent(BookingCompletedEvent event) {
        log.info("Handling BookingCompletedEvent for bookingId: {}", event.bookingId());

        if (payoutRecordRepository.findByBookingId(event.bookingId()).isPresent()) {
            log.warn("PayoutRecord already exists for bookingId: {}. Skipping to ensure idempotency.", event.bookingId());
            return;
        }

        OrderSnapshot order = orderService.getOrderSnapshot(event.orderId());

        // Simple mock calculation for mentor's cut: 80% of total amount
        BigDecimal mentorCut = order.totalAmount().multiply(new BigDecimal("0.80"));

        PayoutRecord record = new PayoutRecord();
        record.setBookingId(event.bookingId());
        record.setMentorId(event.mentorId());
        record.setSourceEventId(UUID.randomUUID()); // In reality, domain events could carry their own IDs
        record.setAmount(mentorCut);
        record.setStatus("PENDING");

        payoutRecordRepository.save(record);
        log.info("Successfully created PayoutRecord for bookingId: {}, amount: {}", event.bookingId(), mentorCut);
    }
}
