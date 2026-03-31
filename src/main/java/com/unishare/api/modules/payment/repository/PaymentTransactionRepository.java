package com.unishare.api.modules.payment.repository;

import com.unishare.api.modules.payment.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    List<PaymentTransaction> findByOrderId(Long orderId);
    Optional<PaymentTransaction> findByTransactionRef(String transactionRef);
}
