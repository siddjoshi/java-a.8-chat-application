package com.example.anonymouschat.service;

import com.example.anonymouschat.model.ChatMessage;
import com.example.anonymouschat.model.ChatUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    private static final long INACTIVITY_TIMEOUT = 30 * 60 * 1000; // 30 minutes

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Store active users by session ID
    private final Map<String, ChatUser> activeUsers = new ConcurrentHashMap<>();
    
    // Queue for users waiting to be paired
    private final Queue<String> waitingQueue = new ConcurrentLinkedQueue<>();
    
    // Store chat rooms (roomId -> Set of userIds)
    private final Map<String, Set<String>> chatRooms = new ConcurrentHashMap<>();

    public String addUser(String sessionId) {
        String userId = generateUserId();
        ChatUser user = new ChatUser(userId, sessionId);
        activeUsers.put(sessionId, user);
        
        logger.info("User {} added with session {}", userId, sessionId);
        
        // Try to pair the user
        String roomId = findOrCreateRoom(sessionId);
        
        if (roomId != null) {
            user.setRoomId(roomId);
            user.setState(ChatUser.UserState.PAIRED);
            notifyUserPaired(sessionId, roomId);
        } else {
            user.setState(ChatUser.UserState.WAITING);
            waitingQueue.offer(sessionId);
            notifyUserWaiting(sessionId);
        }
        
        return userId;
    }

    public void removeUser(String sessionId) {
        ChatUser user = activeUsers.remove(sessionId);
        if (user != null) {
            logger.info("User {} removed with session {}", user.getUserId(), sessionId);
            
            // Remove from waiting queue
            waitingQueue.remove(sessionId);
            
            // Handle room cleanup
            if (user.getRoomId() != null) {
                handleUserDisconnection(user.getRoomId(), sessionId);
            }
        }
    }

    public void sendMessage(String sessionId, ChatMessage message) {
        ChatUser sender = activeUsers.get(sessionId);
        if (sender == null || sender.getState() != ChatUser.UserState.PAIRED) {
            sendErrorMessage(sessionId, "You are not connected to a chat partner");
            return;
        }

        sender.updateActivity();
        message.setSender(sender.getUserId());
        message.setRoomId(sender.getRoomId());
        message.setTimestamp(System.currentTimeMillis());

        // Validate message content
        if (message.getContent() == null || message.getContent().trim().isEmpty()) {
            return;
        }

        // Sanitize message content
        message.setContent(sanitizeMessage(message.getContent()));

        // Send to other user in the room
        Set<String> roomUsers = chatRooms.get(sender.getRoomId());
        if (roomUsers != null) {
            for (String userId : roomUsers) {
                ChatUser user = findUserById(userId);
                if (user != null && !user.getSessionId().equals(sessionId)) {
                    messagingTemplate.convertAndSendToUser(
                        user.getSessionId(), 
                        "/queue/messages", 
                        message
                    );
                }
            }
        }

        logger.info("Message sent from {} in room {}: {}", 
                   sender.getUserId(), sender.getRoomId(), message.getContent());
    }

    public void sendTypingIndicator(String sessionId) {
        ChatUser sender = activeUsers.get(sessionId);
        if (sender == null || sender.getState() != ChatUser.UserState.PAIRED) {
            return;
        }

        sender.updateActivity();
        
        ChatMessage typingMessage = new ChatMessage(
            ChatMessage.MessageType.TYPING, 
            "", 
            sender.getUserId(), 
            sender.getRoomId()
        );

        // Send to other user in the room
        Set<String> roomUsers = chatRooms.get(sender.getRoomId());
        if (roomUsers != null) {
            for (String userId : roomUsers) {
                ChatUser user = findUserById(userId);
                if (user != null && !user.getSessionId().equals(sessionId)) {
                    messagingTemplate.convertAndSendToUser(
                        user.getSessionId(), 
                        "/queue/messages", 
                        typingMessage
                    );
                }
            }
        }
    }

    public void findNewPartner(String sessionId) {
        ChatUser user = activeUsers.get(sessionId);
        if (user == null) {
            return;
        }

        // Leave current room if paired
        if (user.getRoomId() != null) {
            handleUserDisconnection(user.getRoomId(), sessionId);
        }

        // Reset user state
        user.setRoomId(null);
        user.setState(ChatUser.UserState.WAITING);
        user.updateActivity();

        // Try to find new partner
        String roomId = findOrCreateRoom(sessionId);
        
        if (roomId != null) {
            user.setRoomId(roomId);
            user.setState(ChatUser.UserState.PAIRED);
            notifyUserPaired(sessionId, roomId);
        } else {
            waitingQueue.offer(sessionId);
            notifyUserWaiting(sessionId);
        }
    }

    public int getOnlineUsersCount() {
        return activeUsers.size();
    }

    private String findOrCreateRoom(String sessionId) {
        // Try to pair with someone from waiting queue
        String waitingSessionId = waitingQueue.poll();
        
        if (waitingSessionId != null && !waitingSessionId.equals(sessionId)) {
            ChatUser waitingUser = activeUsers.get(waitingSessionId);
            if (waitingUser != null && waitingUser.getState() == ChatUser.UserState.WAITING) {
                // Create new room
                String roomId = generateRoomId();
                Set<String> roomUsers = new HashSet<>();
                roomUsers.add(activeUsers.get(sessionId).getUserId());
                roomUsers.add(waitingUser.getUserId());
                chatRooms.put(roomId, roomUsers);
                
                // Update waiting user
                waitingUser.setRoomId(roomId);
                waitingUser.setState(ChatUser.UserState.PAIRED);
                notifyUserPaired(waitingSessionId, roomId);
                
                logger.info("Room {} created with users {} and {}", 
                           roomId, activeUsers.get(sessionId).getUserId(), waitingUser.getUserId());
                
                return roomId;
            }
        }
        
        return null;
    }

    private void handleUserDisconnection(String roomId, String sessionId) {
        Set<String> roomUsers = chatRooms.get(roomId);
        if (roomUsers != null) {
            ChatUser disconnectingUser = activeUsers.get(sessionId);
            roomUsers.remove(disconnectingUser != null ? disconnectingUser.getUserId() : null);
            
            // Notify other users in the room
            for (String userId : roomUsers) {
                ChatUser user = findUserById(userId);
                if (user != null) {
                    user.setRoomId(null);
                    user.setState(ChatUser.UserState.WAITING);
                    
                    ChatMessage disconnectMessage = new ChatMessage(
                        ChatMessage.MessageType.DISCONNECT,
                        "Your chat partner has disconnected. Looking for a new partner...",
                        "System",
                        roomId
                    );
                    
                    messagingTemplate.convertAndSendToUser(
                        user.getSessionId(), 
                        "/queue/messages", 
                        disconnectMessage
                    );
                    
                    // Try to find new partner for remaining user
                    findNewPartner(user.getSessionId());
                }
            }
            
            // Remove the room
            chatRooms.remove(roomId);
            logger.info("Room {} closed due to user disconnection", roomId);
        }
    }

    private void notifyUserPaired(String sessionId, String roomId) {
        ChatMessage pairedMessage = new ChatMessage(
            ChatMessage.MessageType.USER_PAIRED,
            "You are now connected to a chat partner! Start chatting...",
            "System",
            roomId
        );
        
        messagingTemplate.convertAndSendToUser(sessionId, "/queue/messages", pairedMessage);
    }

    private void notifyUserWaiting(String sessionId) {
        ChatMessage waitingMessage = new ChatMessage(
            ChatMessage.MessageType.WAITING,
            "Looking for a chat partner... Please wait.",
            "System",
            null
        );
        
        messagingTemplate.convertAndSendToUser(sessionId, "/queue/messages", waitingMessage);
    }

    private void sendErrorMessage(String sessionId, String errorText) {
        ChatMessage errorMessage = new ChatMessage(
            ChatMessage.MessageType.ERROR,
            errorText,
            "System",
            null
        );
        
        messagingTemplate.convertAndSendToUser(sessionId, "/queue/messages", errorMessage);
    }

    private ChatUser findUserById(String userId) {
        return activeUsers.values().stream()
                .filter(user -> user.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    private String generateUserId() {
        return "User-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateRoomId() {
        return "Room-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String sanitizeMessage(String message) {
        if (message == null) {
            return "";
        }
        
        // Basic XSS protection
        return message.replaceAll("<", "&lt;")
                     .replaceAll(">", "&gt;")
                     .replaceAll("\"", "&quot;")
                     .replaceAll("'", "&#x27;")
                     .trim()
                     .substring(0, Math.min(message.length(), 500)); // Limit message length
    }

    // Cleanup inactive users (should be called periodically)
    public void cleanupInactiveUsers() {
        List<String> inactiveSessionIds = new ArrayList<>();
        
        for (Map.Entry<String, ChatUser> entry : activeUsers.entrySet()) {
            if (entry.getValue().isInactive(INACTIVITY_TIMEOUT)) {
                inactiveSessionIds.add(entry.getKey());
            }
        }
        
        for (String sessionId : inactiveSessionIds) {
            removeUser(sessionId);
            logger.info("Removed inactive user with session {}", sessionId);
        }
    }
}
