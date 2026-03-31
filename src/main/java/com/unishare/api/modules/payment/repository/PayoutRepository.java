package com.unishare.api.modules.payment.repository;

import com.unishare.api.modules.payment.entity.Payout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayoutRepository extends JpaRepository<Payout, Long> {
    List<Payout> findByUserId(Long userId);
}
