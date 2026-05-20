package com.peddex.demo.game;

public class GameMove {
    private String playerId;
    private Integer column; // 0 to 6

    public GameMove() {
    }

    public GameMove(String playerId, Integer column) {
        this.playerId = playerId;
        this.column = column;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public Integer getColumn() {
        return column;
    }

    public void setColumn(Integer column) {
        this.column = column;
    }
}
