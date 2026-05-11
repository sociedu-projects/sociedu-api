package com.unishare.api.modules.chat.websocket;

import com.unishare.api.modules.chat.dto.ChatMessageResponse;
import com.unishare.api.modules.chat.dto.SendMessageRequest;
import com.unishare.api.modules.chat.dto.WsSendMessageRequest;
import com.unishare.api.modules.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ChatWsController {

    private final ChatService chatService;

    @MessageMapping("/chat.send")
    public ChatMessageResponse send(@Valid @Payload WsSendMessageRequest request, Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        SendMessageRequest messageRequest = new SendMessageRequest();
        messageRequest.setContent(request.getContent());
        messageRequest.setType(request.getType());
        messageRequest.setAttachmentFileIds(request.getAttachmentFileIds());
        return chatService.sendMessage(userId, request.getConversationId(), messageRequest);
    }
}
