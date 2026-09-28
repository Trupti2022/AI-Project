package com.store.taskmanager.config;

import com.store.taskmanager.websocket.TaskWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final TaskWebSocketHandler taskWebSocketHandler;

    public WebSocketConfig(TaskWebSocketHandler taskWebSocketHandler) {
        this.taskWebSocketHandler = taskWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(taskWebSocketHandler, "/ws/tasks")
                .setAllowedOrigins("*");
    }
}
