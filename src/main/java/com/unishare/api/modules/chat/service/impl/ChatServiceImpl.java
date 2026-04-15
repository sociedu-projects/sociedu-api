package com.unishare.api.modules.chat.service.impl;

import com.unishare.api.common.dto.AppException;
import com.unishare.api.modules.chat.dto.*;
import com.unishare.api.modules.chat.entity.ChatMessage;
import com.unishare.api.modules.chat.entity.Conversation;
import com.unishare.api.modules.chat.entity.ConversationParticipant;
import com.unishare.api.modules.chat.entity.ConversationParticipantId;
import com.unishare.api.modules.chat.entity.MessageAttachment;
import com.unishare.api.modules.chat.exception.ChatErrorCode;
import com.unishare.api.modules.chat.repository.ChatMessageRepository;
import com.unishare.api.modules.chat.repository.ConversationParticipantRepository;
import com.unishare.api.modules.chat.repository.ConversationRepository;
import com.unishare.api.modules.chat.repository.MessageAttachmentRepository;
import com.unishare.api.modules.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final ChatMessageRepository messageRepository;
    private final MessageAttachmentRepository attachmentRepository;

    @Override
    @Transactional
    public ConversationResponse createConversation(Long creatorUserId, CreateConversationRequest request) {
        Set<Long> users = new LinkedHashSet<>(request.getParticipantUserIds());
        users.add(creatorUserId);

        Conversation c = new Conversation();
        c.setType(request.getType());
        c.setBookingId(request.getBookingId());
        c = conversationRepository.save(c);

        Long cid = c.getId();
        for (Long uid : users) {
            ConversationParticipant p = new ConversationParticipant();
            p.setId(new ConversationParticipantId(cid, uid));
            participantRepository.save(p);
        }

        return toConvResponse(c);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> listMyConversations(Long userId) {
        List<Long> ids = participantRepository.findConversationIdsByUserId(userId);
        return conversationRepository.findAllById(ids).stream()
                .map(this::toConvResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> listMessages(Long userId, Long conversationId) {
        assertParticipant(conversationId, userId);
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId).stream()
                .map(this::toMessageResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(Long userId, Long conversationId, SendMessageRequest request) {
        assertParticipant(conversationId, userId);
        ChatMessage m = new ChatMessage();
        m.setConversationId(conversationId);
        m.setSenderId(userId);
        m.setContent(request.getContent());
        m.setType(request.getType() != null ? request.getType() : "text");
        m = messageRepository.save(m);

        if (request.getAttachmentFileIds() != null) {
            for (Long fid : request.getAttachmentFileIds()) {
                MessageAttachment a = new MessageAttachment();
                a.setMessageId(m.getId());
                a.setFileId(fid);
                attachmentRepository.save(a);
            }
        }
        return toMessageResponse(m);
    }

    private void assertParticipant(Long conversationId, Long userId) {
        conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ChatErrorCode.CONVERSATION_NOT_FOUND));
        if (!participantRepository.isParticipant(conversationId, userId)) {
            throw new AppException(ChatErrorCode.CHAT_ACCESS_DENIED);
        }
    }

    private ConversationResponse toConvResponse(Conversation c) {
        return ConversationResponse.builder()
                .id(c.getId())
                .type(c.getType())
                .bookingId(c.getBookingId())
                .createdAt(c.getCreatedAt())
                .build();
    }

    private ChatMessageResponse toMessageResponse(ChatMessage m) {
        List<Long> fileIds = attachmentRepository.findByMessageId(m.getId()).stream()
                .map(MessageAttachment::getFileId)
                .collect(Collectors.toList());
        return ChatMessageResponse.builder()
                .id(m.getId())
                .senderId(m.getSenderId())
                .content(m.getContent())
                .type(m.getType())
                .edited(m.getEdited())
                .createdAt(m.getCreatedAt())
                .attachmentFileIds(fileIds.isEmpty() ? null : fileIds)
                .build();
    }
}
