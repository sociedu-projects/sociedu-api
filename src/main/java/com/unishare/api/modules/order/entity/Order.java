package com.unishare.api.modules.order.entity;

import com.unishare.api.common.constants.OrderStatuses;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "buyer_id", nullable = false)
    private UUID buyerId;

    /** ID phiên bản gói dịch vụ (service_package_versions.id) — thống nhất với schema. */
    @Column(name = "service_id", nullable = false)
    private UUID serviceId;

    @Column(name = "status", nullable = false)
    private String status = OrderStatuses.PENDING_PAYMENT;

    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }
}
