package com.peddex.demo.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EchoWebSocketHandler extends TextWebSocketHandler {

    // 1. Thread-safe list to hold all connected players
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    
    // 2. Jackson ObjectMapper to easily read/write JSON
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session); // Add player to the lobby
        System.out.println("Client connected: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            // Parse incoming JSON string into an object
            JsonNode jsonNode = objectMapper.readTree(message.getPayload());
            String type = jsonNode.has("type") ? jsonNode.get("type").asText() : "";

            if ("join".equals(type)) {
                // The handshake: save their username to their session
                String username = jsonNode.has("username") ? jsonNode.get("username").asText() : "Anonymous";
                session.getAttributes().put("username", username);
                
                // Broadcast to EVERYONE that someone joined
                String joinMessage = objectMapper.writeValueAsString(
                        new ChatMessage("system", username + " has joined the chat.")
                );
                broadcast(joinMessage);
                
            } else if ("chat".equals(type)) {
                // Get the sender's username from their session
                String username = (String) session.getAttributes().getOrDefault("username", "Anonymous");
                String content = jsonNode.has("content") ? jsonNode.get("content").asText() : "";
                
                // Broadcast their chat message to EVERYONE
                String chatMessage = objectMapper.writeValueAsString(
                        new ChatMessage("chat", content, username)
                );
                broadcast(chatMessage);
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session); // Remove from lobby
        
        String username = (String) session.getAttributes().get("username");
        if (username != null) {
            String leaveMessage = objectMapper.writeValueAsString(
                    new ChatMessage("system", username + " has left the chat.")
            );
            broadcast(leaveMessage);
        }
        System.out.println("Client disconnected: " + session.getId());
    }

    // Helper method to loop through all connected players and send them a message
    private void broadcast(String messagePayload) {
        TextMessage textMessage = new TextMessage(messagePayload);
        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                }
            } catch (IOException e) {
                System.err.println("Error broadcasting to session: " + e.getMessage());
            }
        }
    }

    // Simple DTO class to help build JSON responses cleanly
    private static class ChatMessage {
        public String type;
        public String content;
        public String username;

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
}
