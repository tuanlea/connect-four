package com.peddex.demo.websocket.model;

public class MoveMessage {
    public String type;
    public String username;
    public int x;
    public int y;

    public MoveMessage() {}

    public MoveMessage(String type, String username, int x, int y) {
        this.type = type;
        this.username = username;
        this.x = x;
        this.y = y;
    }
}
