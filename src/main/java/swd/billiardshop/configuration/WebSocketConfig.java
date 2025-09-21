package swd.billiardshop.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Thêm handler thực tế nếu có, ví dụ:
        // registry.addHandler(new YourWebSocketHandler(), "/ws/your-endpoint").setAllowedOrigins("*").withSockJS();
    }
}
