package com.unishare.api.modules.chat.websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

public class ChatPrincipalHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(
            ServerHttpRequest request,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        Object principal = attributes.get("chatPrincipal");
        if (principal instanceof Principal p) {
            return p;
        }
        return super.determineUser(request, wsHandler, attributes);
    }
}
