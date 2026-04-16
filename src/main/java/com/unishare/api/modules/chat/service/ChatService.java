package com.unishare.api.modules.chat.service;

import com.unishare.api.modules.chat.dto.*;

import java.util.List;
import java.util.UUID;

public interface ChatService {

    ConversationResponse createConversation(UUID creatorUserId, CreateConversationRequest request);

    List<ConversationResponse> listMyConversations(UUID userId);

    List<ChatMessageResponse> listMessages(UUID userId, UUID conversationId);

    ChatMessageResponse sendMessage(UUID userId, UUID conversationId, SendMessageRequest request);
}
