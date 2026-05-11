package com.unishare.api.modules.chat.websocket;

import com.unishare.api.modules.chat.repository.ConversationParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChatInboundChannelInterceptor implements ChannelInterceptor {

    private static final String TOPIC_PREFIX = "/topic/conversations/";
    private final ConversationParticipantRepository participantRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand()) && accessor.getUser() == null) {
            Object principal = accessor.getSessionAttributes() != null
                    ? accessor.getSessionAttributes().get("chatPrincipal")
                    : null;
            if (principal instanceof Principal p) {
                accessor.setUser(p);
            }
        }

        if (!StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            return message;
        }
        Principal principal = accessor.getUser();
        String destination = accessor.getDestination();
        if (principal == null || destination == null || !destination.startsWith(TOPIC_PREFIX)) {
            return message;
        }

        UUID userId = UUID.fromString(principal.getName());
        UUID conversationId = UUID.fromString(destination.substring(TOPIC_PREFIX.length()));
        if (!participantRepository.isParticipant(conversationId, userId)) {
            throw new IllegalArgumentException("User is not a participant of this conversation.");
        }
        return message;
    }
}
