package swd.billiardshop.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint cho WebSocket connection
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")  // Cho phép mọi origin (production nên chỉ định cụ thể)
                .withSockJS();                   // Fallback cho browsers không support WebSocket
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefix cho các message từ server gửi đến client
        registry.enableSimpleBroker("/topic", "/queue");

        // Prefix cho các message từ client gửi lên server
        registry.setApplicationDestinationPrefixes("/app");

        // Prefix cho user-specific messages
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * Cấu hình buffer size cho WebSocket
     */
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(8192);
        container.setMaxBinaryMessageBufferSize(8192);
        return container;
    }
}