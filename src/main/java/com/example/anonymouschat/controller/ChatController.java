package com.example.anonymouschat.controller;

import com.example.anonymouschat.model.ChatMessage;
import com.example.anonymouschat.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Controller
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private ChatService chatService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessage chatMessage,
                           SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        logger.info("Received message from session {}: {}", sessionId, chatMessage);
        
        chatMessage.setType(ChatMessage.MessageType.CHAT);
        chatService.sendMessage(sessionId, chatMessage);
    }

    @MessageMapping("/chat.typing")
    public void sendTypingIndicator(SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        chatService.sendTypingIndicator(sessionId);
    }

    @MessageMapping("/chat.findNew")
    public void findNewPartner(SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        logger.info("User requesting new partner from session {}", sessionId);
        chatService.findNewPartner(sessionId);
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        logger.info("Received a new web socket connection with session ID: {}", sessionId);
        
        String userId = chatService.addUser(sessionId);
        logger.info("User {} connected with session {}", userId, sessionId);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        if (sessionId != null) {
            logger.info("User disconnected with session ID: {}", sessionId);
            chatService.removeUser(sessionId);
        }
    }
}
