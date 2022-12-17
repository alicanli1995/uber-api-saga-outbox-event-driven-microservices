package com.uber.api.customer.service.ws;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.uber.api.common.api.event.BusinessEvent;
import com.uber.api.common.api.event.UberWebSocketEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class InformationWebsocketService implements WebSocketHandler, UberWebSocketEvent {

    private final ObjectMapper objectMapper;
    private final Map<String , WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        log.info("New session is established: {}", session.getId());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {

    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("Error has occurred while transferring data using websocket: {}", exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        sessions.remove(session.getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }


    @Override
    public void publishEvent(BusinessEvent event) {
        sessions.forEach((id, session) -> {
            try {
                var jsonEvent = objectMapper.writeValueAsString(event);
                session.sendMessage(new TextMessage(jsonEvent));
                log.info("Event has been sent to client: {}", jsonEvent);
            } catch (Exception e) {
                log.error("Error has occurred while transferring data using websocket: {}", e.getMessage());
            }
        });
    }
}
