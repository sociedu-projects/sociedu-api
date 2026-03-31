package com.unishare.api.modules.mentor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "mentor_profiles")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class MentorProfile {

    @Id
    @Column(name = "user_id")
    private Long userId;

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
    private String verificationStatus = "pending";

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
}
