package com.unishare.api.modules.order.listener;

import com.unishare.api.infrastructure.event.DomainEventPublisher;
import com.unishare.api.common.event.PaymentProcessedEvent;
import com.unishare.api.common.event.PaymentSucceededEvent;
import com.unishare.api.modules.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Payment chỉ bắn {@link PaymentProcessedEvent}; sau khi commit giao dịch thanh toán, listener này cập nhật trạng thái đơn
 * và (khi thành công) bắn thêm {@link PaymentSucceededEvent} có mã giao dịch cổng.
 */
@Component
@RequiredArgsConstructor
public class PaymentProcessedEventListener {

    private final OrderService orderService;
    private final DomainEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentProcessed(PaymentProcessedEvent event) {
        orderService.applyPaymentResult(event.orderId(), event.success());
        if (event.success()) {
            var snap = orderService.getOrderSnapshot(event.orderId());
            eventPublisher.publish(new PaymentSucceededEvent(
                    event.orderId(),
                    snap.buyerId(),
                    event.provider(),
                    event.providerTransactionId(),
                    event.paymentTransactionId()));
        }
    }
}
