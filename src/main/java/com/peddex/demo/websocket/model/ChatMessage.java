package com.peddex.demo.websocket.model;

public class ChatMessage {
    public String type;
    public String content;
    public String username;

    public ChatMessage() {}

    public ChatMessage(String type, String content) {
        this.type = type;
        this.content = content;
    }

    public ChatMessage(String type, String content, String username) {
        this.type = type;
        this.content = content;
        this.username = username;
    }
}
