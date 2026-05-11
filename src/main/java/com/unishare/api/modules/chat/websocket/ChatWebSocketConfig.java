package com.unishare.api.modules.chat.websocket;

import com.unishare.api.config.AppUrlsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.util.CollectionUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class ChatWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final AppUrlsProperties appUrlsProperties;
    private final ChatHandshakeInterceptor chatHandshakeInterceptor;
    private final ChatInboundChannelInterceptor chatInboundChannelInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        List<String> allowedOrigins = appUrlsProperties.corsAllowedOrigins();
        String[] origins = CollectionUtils.isEmpty(allowedOrigins) ? new String[0] : allowedOrigins.toArray(String[]::new);
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns(origins)
                .addInterceptors(chatHandshakeInterceptor)
                .setHandshakeHandler(new ChatPrincipalHandshakeHandler())
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(chatInboundChannelInterceptor);
    }
}
