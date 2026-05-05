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

import com.peddex.demo.websocket.model.ChatMessage;
import com.peddex.demo.websocket.model.MoveMessage;

public class EchoWebSocketHandler extends TextWebSocketHandler {

    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("Client connected: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            JsonNode jsonNode = objectMapper.readTree(message.getPayload());
            String type = jsonNode.has("type") ? jsonNode.get("type").asText() : "";

            if ("join".equals(type)) {
                String username = jsonNode.has("username") ? jsonNode.get("username").asText() : "Anonymous";
                session.getAttributes().put("username", username);
                
                String joinMessage = objectMapper.writeValueAsString(
                        new ChatMessage("system", username + " has joined the chat.")
                );
                broadcast(joinMessage);
                
                // --- SEND EXISTING PLAYERS TO THE NEW CLIENT ---
                for (WebSocketSession s : sessions) {
                    if (s != session && s.isOpen()) {
                        String existingUser = (String) s.getAttributes().get("username");
                        Integer exX = (Integer) s.getAttributes().get("x");
                        Integer exY = (Integer) s.getAttributes().get("y");
                        
                        if (existingUser != null && exX != null && exY != null) {
                            String stateMessage = objectMapper.writeValueAsString(
                                    new MoveMessage("move", existingUser, exX, exY)
                            );
                            session.sendMessage(new TextMessage(stateMessage));
                        }
                    }
                }
                
            } else if ("chat".equals(type)) {
                String username = (String) session.getAttributes().getOrDefault("username", "Anonymous");
                String content = jsonNode.has("content") ? jsonNode.get("content").asText() : "";
                
                String chatMessage = objectMapper.writeValueAsString(
                        new ChatMessage("chat", content, username)
                );
                broadcast(chatMessage);
                
            } else if ("move".equals(type)) {
                // Handle Movement sync
                String username = (String) session.getAttributes().getOrDefault("username", "Anonymous");
                int x = jsonNode.has("x") ? jsonNode.get("x").asInt() : 0;
                int y = jsonNode.has("y") ? jsonNode.get("y").asInt() : 0;
                
                // Save true coordinates to session state
                session.getAttributes().put("x", x);
                session.getAttributes().put("y", y);
                
                // Broadcast movement to all clients
                String moveMessage = objectMapper.writeValueAsString(
                        new MoveMessage("move", username, x, y)
                );
                broadcast(moveMessage);
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        
        String username = (String) session.getAttributes().get("username");
        if (username != null) {
            // We pass the username so the frontend knows who exactly to remove from the game board!
            String leaveMessage = objectMapper.writeValueAsString(
                    new ChatMessage("system", username + " has left the chat.", username)
            );
            broadcast(leaveMessage);
        }
        System.out.println("Client disconnected: " + session.getId());
    }

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
}
