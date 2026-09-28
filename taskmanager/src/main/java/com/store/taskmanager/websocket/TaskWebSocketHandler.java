package com.store.taskmanager.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.taskmanager.model.Task;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TaskWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String associateId = extractAssociateId(session);
        if (associateId != null) {
            sessions.put(associateId, session);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session,
            org.springframework.web.socket.CloseStatus status) {
        sessions.values().remove(session);
    }

    public void broadcastToAssociate(String associateId, List<Task> tasks) {
        WebSocketSession session = sessions.get(associateId);
        if (session != null && session.isOpen()) {
            try {
                String payload = mapper.writeValueAsString(
                    Map.of("type", "TASK_UPDATE", "tasks", tasks)
                );
                session.sendMessage(new TextMessage(payload));
            } catch (Exception e) {
                // session closed
            }
        }
    }

    private String extractAssociateId(WebSocketSession session) {
        String query = session.getUri() != null ? session.getUri().getQuery() : null;
        if (query != null && query.startsWith("associateId=")) {
            return query.substring("associateId=".length());
        }
        return null;
    }
}