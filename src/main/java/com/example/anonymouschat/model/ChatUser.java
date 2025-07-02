package com.example.anonymouschat.model;

public class ChatUser {
    private String userId;
    private String sessionId;
    private String roomId;
    private UserState state;
    private long lastActivity;

    public ChatUser() {
        this.lastActivity = System.currentTimeMillis();
        this.state = UserState.WAITING;
    }

    public ChatUser(String userId, String sessionId) {
        this();
        this.userId = userId;
        this.sessionId = sessionId;
    }

    public enum UserState {
        WAITING,
        PAIRED,
        DISCONNECTED
    }

    public void updateActivity() {
        this.lastActivity = System.currentTimeMillis();
    }

    public boolean isInactive(long timeoutMs) {
        return System.currentTimeMillis() - lastActivity > timeoutMs;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public UserState getState() {
        return state;
    }

    public void setState(UserState state) {
        this.state = state;
    }

    public long getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(long lastActivity) {
        this.lastActivity = lastActivity;
    }

    @Override
    public String toString() {
        return "ChatUser{" +
                "userId='" + userId + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", roomId='" + roomId + '\'' +
                ", state=" + state +
                ", lastActivity=" + lastActivity +
                '}';
    }
}
