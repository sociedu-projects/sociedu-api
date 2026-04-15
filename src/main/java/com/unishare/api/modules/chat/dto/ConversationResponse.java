package com.unishare.api.modules.chat.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ConversationResponse {
    private Long id;
    private String type;
    private Long bookingId;
    private Instant createdAt;
}
