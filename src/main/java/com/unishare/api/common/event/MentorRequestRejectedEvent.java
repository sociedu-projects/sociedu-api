package com.unishare.api.common.event;

import java.util.UUID;

/**
 * Phát khi admin reject đơn mentor. Listener sẽ gửi email + notification cho user.
 */
public record MentorRequestRejectedEvent(UUID requestId,
                                         UUID userId,
                                         UUID rejectedBy,
                                         String reason,
                                         String note) implements DomainEvent {}
