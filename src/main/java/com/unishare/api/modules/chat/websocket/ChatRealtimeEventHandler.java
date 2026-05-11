package com.unishare.api.modules.chat.websocket;

import com.unishare.api.modules.chat.event.ChatMessageSentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatRealtimeEventHandler {

    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void onChatMessageSent(ChatMessageSentEvent event) {
        String destination = "/topic/conversations/" + event.getConversationId();
        messagingTemplate.convertAndSend(destination, event.getMessage());
    }
}
