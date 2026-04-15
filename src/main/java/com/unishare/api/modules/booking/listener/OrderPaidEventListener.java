package com.unishare.api.modules.booking.listener;

import com.unishare.api.modules.booking.service.BookingService;
import com.unishare.api.common.event.OrderPaidEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderPaidEventListener {

    private final BookingService bookingService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPaid(OrderPaidEvent event) {
        bookingService.ensureBookingForOrder(event.orderId());
    }
}
