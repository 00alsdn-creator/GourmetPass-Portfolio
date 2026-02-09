package com.uhi.gourmet.common.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 1. 클라이언트(JSP)에서 접속할 엔드포인트 설정: /ws_waiting
        // SockJS를 지원하여 웹소켓을 지원하지 않는 브라우저에서도 동작하게 함
        registry.addEndpoint("/ws_waiting").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 2. 서버에서 클라이언트로 메시지를 보낼 때 사용할 접두사 설정: /topic
        // WaitController의 messaging_template.convertAndSend("/topic/wait/...")와 연결됨
        config.enableSimpleBroker("/topic");
        
        // 3. 클라이언트에서 서버로 메시지를 보낼 때 사용할 접두사 설정 (현재는 알림 수신 위주이므로 /app 사용)
        config.setApplicationDestinationPrefixes("/app");
    }
}
