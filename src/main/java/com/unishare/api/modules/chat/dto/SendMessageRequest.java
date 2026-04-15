package com.unishare.api.modules.chat.dto;

import com.unishare.api.common.constants.MessageTypes;
import lombok.Data;

import java.util.List;

@Data
public class SendMessageRequest {
    private String content;
    private String type = MessageTypes.TEXT;
    private List<Long> attachmentFileIds;
}
