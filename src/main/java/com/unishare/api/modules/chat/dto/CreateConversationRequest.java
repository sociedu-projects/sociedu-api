package com.unishare.api.modules.chat.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateConversationRequest {

    @NotNull
    private String type;

    private Long bookingId;

    @NotEmpty
    private List<Long> participantUserIds;
}
