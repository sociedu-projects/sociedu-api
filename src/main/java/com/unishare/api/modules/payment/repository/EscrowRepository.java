package com.unishare.api.modules.payment.repository;

import com.unishare.api.modules.payment.entity.Escrow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EscrowRepository extends JpaRepository<Escrow, Long> {
    Optional<Escrow> findByOrderId(Long orderId);
}
