package com.unishare.api.modules.service.entity;

import com.unishare.api.common.constants.MentorVerificationStatuses;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "mentor_profiles")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class MentorProfile {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    private String headline;

    @Column(columnDefinition = "TEXT")
    private String expertise; // comma separated

    @Column(name = "base_price")
    private BigDecimal basePrice;

    @Column(name = "rating_avg")
    private Float ratingAvg = 0f;

    @Column(name = "sessions_completed")
    private Integer sessionsCompleted = 0;

    @Column(name = "verification_status")
    private String verificationStatus = MentorVerificationStatuses.PENDING;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
}
