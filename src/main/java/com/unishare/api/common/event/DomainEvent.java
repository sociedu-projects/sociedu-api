package com.unishare.api.common.event;

import com.unishare.api.infrastructure.event.DomainEventPublisher;

/**
 * Kiểu chung cho mọi event phát qua {@link DomainEventPublisher}.
 * Record/event mới trong module chỉ cần {@code implements DomainEvent} để type-safe và dễ mở rộng
 * (Kafka, outbox, logging) tại một implementation.
 */
public interface DomainEvent {}
