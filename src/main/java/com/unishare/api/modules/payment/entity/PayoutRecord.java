package com.unishare.api.modules.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payout_records", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"booking_id"}),
        @UniqueConstraint(columnNames = {"source_event_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class PayoutRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    @Column(name = "mentor_id", nullable = false)
    private UUID mentorId;

    @Column(name = "source_event_id", nullable = false)
    private UUID sourceEventId;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, PROCESSING, SUCCESS, FAILED, RETRYING

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Version
    private Long version;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
