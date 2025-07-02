package com.example.anonymouschat.model;

public class ChatMessage {
    private MessageType type;
    private String content;
    private String sender;
    private String roomId;
    private long timestamp;

    public ChatMessage() {
        this.timestamp = System.currentTimeMillis();
    }

    public ChatMessage(MessageType type, String content, String sender, String roomId) {
        this();
        this.type = type;
        this.content = content;
        this.sender = sender;
        this.roomId = roomId;
    }

    public enum MessageType {
        CONNECT,
        CHAT,
        DISCONNECT,
        TYPING,
        USER_PAIRED,
        WAITING,
        ERROR
    }

    // Getters and Setters
    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "type=" + type +
                ", content='" + content + '\'' +
                ", sender='" + sender + '\'' +
                ", roomId='" + roomId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
