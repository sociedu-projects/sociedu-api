package com.unishare.api.modules.chat.controller;

import com.unishare.api.common.dto.ApiResponse;
import com.unishare.api.config.OpenApiConfig;
import com.unishare.api.infrastructure.security.CustomUserPrincipal;
import com.unishare.api.modules.chat.dto.*;
import com.unishare.api.modules.chat.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
@Tag(name = "Chat")
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "Tạo conversation")
    @PreAuthorize("hasAuthority(T(com.unishare.api.common.constants.Capabilities).VIEW_CONVERSATION)")
    @PostMapping("/conversations")
    public ResponseEntity<ApiResponse<ConversationResponse>> create(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody CreateConversationRequest request) {
        return ResponseEntity.ok(ApiResponse.<ConversationResponse>build()
                .withData(chatService.createConversation(principal.getUserId(), request)));
    }

    @Operation(summary = "Danh sách conversation của tôi")
    @PreAuthorize("hasAuthority(T(com.unishare.api.common.constants.Capabilities).VIEW_CONVERSATION)")
    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> listConversations(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.<List<ConversationResponse>>build()
                .withData(chatService.listMyConversations(principal.getUserId())));
    }

    @Operation(summary = "Tin nhắn trong conversation")
    @PreAuthorize("hasAuthority(T(com.unishare.api.common.constants.Capabilities).VIEW_CONVERSATION)")
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> listMessages(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long conversationId) {
        return ResponseEntity.ok(ApiResponse.<List<ChatMessageResponse>>build()
                .withData(chatService.listMessages(principal.getUserId(), conversationId)));
    }

    @Operation(summary = "Gửi tin nhắn")
    @PreAuthorize("hasAuthority(T(com.unishare.api.common.constants.Capabilities).SEND_MESSAGE)")
    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> send(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long conversationId,
            @RequestBody SendMessageRequest request) {
        return ResponseEntity.ok(ApiResponse.<ChatMessageResponse>build()
                .withData(chatService.sendMessage(principal.getUserId(), conversationId, request)));
    }
}
