package com.unishare.api.modules.chat.event;

import com.unishare.api.modules.chat.dto.ChatMessageResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ChatMessageSentEvent {
    private final UUID conversationId;
    private final ChatMessageResponse message;
}
