package com.unishare.api.modules.chat.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ConversationResponse {
    private UUID id;
    private String type;
    private UUID bookingId;
    private Instant createdAt;
}
