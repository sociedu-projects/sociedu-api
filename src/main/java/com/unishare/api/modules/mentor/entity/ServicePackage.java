package com.unishare.api.modules.mentor.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "service_packages")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ServicePackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mentor_id")
    private Long mentorId;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer duration; // in minutes

    private BigDecimal price;

    @Column(name = "delivery_type")
    private String deliveryType; // e.g., online, chat

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
