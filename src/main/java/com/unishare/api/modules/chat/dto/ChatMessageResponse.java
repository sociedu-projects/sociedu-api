package com.unishare.api.modules.chat.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class ChatMessageResponse {
    private Long id;
    private Long senderId;
    private String content;
    private String type;
    private Boolean edited;
    private Instant createdAt;
    private List<Long> attachmentFileIds;
}
