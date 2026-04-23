package com.unishare.api.common.event;

import java.util.UUID;

/**
 * Phát khi admin approve đơn apply mentor. Listener sẽ tạo {@code MentorProfile},
 * gán role MENTOR, và gửi notification/email cho user.
 */
public record MentorRequestApprovedEvent(UUID requestId,
                                         UUID userId,
                                         UUID approvedBy,
                                         String note) implements DomainEvent {}
