package com.peddex.demo.game;

public class GameState {

    public enum Status {
        WAITING_FOR_PLAYERS,
        IN_PROGRESS,
        PLAYER_1_WON,
        PLAYER_2_WON,
        DRAW
    }

    private int[][] board; // 0 = empty, 1 = player 1, 2 = player 2
    private int currentPlayer; // 1 or 2
    private Status status;
    private String message;
    private String player1Id;
    private String player2Id;

    public GameState() {
        this.board = new int[6][7]; // 6 rows, 7 cols
        this.currentPlayer = 1;
        this.status = Status.IN_PROGRESS;
    }

    public String getPlayer1Id() { return player1Id; }
    public void setPlayer1Id(String player1Id) { this.player1Id = player1Id; }
    public String getPlayer2Id() { return player2Id; }
    public void setPlayer2Id(String player2Id) { this.player2Id = player2Id; }

    public int[][] getBoard() {
        return board;
    }

    public void setBoard(int[][] board) {
        this.board = board;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(int currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
