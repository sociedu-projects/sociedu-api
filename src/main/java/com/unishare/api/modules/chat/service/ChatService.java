package com.unishare.api.modules.chat.service;

import com.unishare.api.modules.chat.dto.*;

import java.util.List;

public interface ChatService {

    ConversationResponse createConversation(Long creatorUserId, CreateConversationRequest request);

    List<ConversationResponse> listMyConversations(Long userId);

    List<ChatMessageResponse> listMessages(Long userId, Long conversationId);

    ChatMessageResponse sendMessage(Long userId, Long conversationId, SendMessageRequest request);
}
