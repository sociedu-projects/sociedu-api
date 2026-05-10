package com.unishare.api.modules.payment.listener;

import com.unishare.api.common.event.SessionCanceledEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class RefundPolicyListener {

    /**
     * Lắng nghe sự kiện Hủy buổi học.
     * Dựa vào thời gian hủy và người hủy để quyết định chính sách hoàn tiền (RefundPolicy).
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleSessionCanceledEvent(SessionCanceledEvent event) {
        log.info("Handling SessionCanceledEvent for bookingId: {}, sessionId: {}", event.bookingId(), event.sessionId());

        // TODO: Implement Refund Policy Logic
        // 1. Check who canceled the session (Mentor or Mentee)
        // 2. Check cancellation time against scheduled time
        // 3. If Mentor canceled early -> Initiate Refund for the session's pro-rated amount
        // 4. If Mentee no-show -> Maybe pay mentor a partial fee
        // 5. Create a RefundRecord or Update Order
        
        log.info("Refund policy triggered for cancellation by user: {}, reason: {}", event.canceledBy(), event.cancelReason());
    }
}
