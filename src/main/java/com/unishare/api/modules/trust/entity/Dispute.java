package com.unishare.api.modules.trust.entity;

import com.unishare.api.common.constants.DisputeStatuses;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "disputes")
@Getter
@Setter
@NoArgsConstructor
public class Dispute {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "report_id")
    private UUID reportId;

    @Column(name = "booking_id")
    private UUID bookingId;

    @Column(name = "session_id")
    private UUID sessionId;

    @Column(name = "raised_by", nullable = false)
    private UUID raisedBy;

    @Column(nullable = false)
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String status = DisputeStatuses.OPEN;

    @Column(name = "resolution_note", columnDefinition = "TEXT")
    private String resolutionNote;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "resolved_by")
    private UUID resolvedBy;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }
}
