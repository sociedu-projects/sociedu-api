package com.unishare.api.modules.chat.dto;

import com.unishare.api.common.constants.MessageTypes;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class WsSendMessageRequest {

    @NotNull
    private UUID conversationId;

    private String content;

    private String type = MessageTypes.TEXT;

    private List<UUID> attachmentFileIds;
}
