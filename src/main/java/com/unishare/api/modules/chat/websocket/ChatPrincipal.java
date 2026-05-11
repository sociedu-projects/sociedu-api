package com.unishare.api.modules.chat.websocket;

import java.security.Principal;
import java.util.UUID;

public class ChatPrincipal implements Principal {

    private final UUID userId;

    public ChatPrincipal(UUID userId) {
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }

    @Override
    public String getName() {
        return userId.toString();
    }
}
