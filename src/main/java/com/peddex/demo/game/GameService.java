package com.peddex.demo.game;

import org.springframework.stereotype.Service;

@Service
public class GameService {

    private GameState gameState = new GameState();

    public GameState getGameState() {
        return gameState;
    }

    public synchronized GameState joinGame(String playerId) {
        if (playerId == null || playerId.isEmpty()) {
            return gameState;
        }
        
        if (gameState.getPlayer1Id() == null) {
            gameState.setPlayer1Id(playerId);
        } else if (gameState.getPlayer2Id() == null && !gameState.getPlayer1Id().equals(playerId)) {
            gameState.setPlayer2Id(playerId);
        }
        return gameState;
    }

    public synchronized GameState makeMove(GameMove move) {
        if (gameState.getStatus() != GameState.Status.IN_PROGRESS) {
            gameState.setMessage("Game is over. Restart to play again.");
            return gameState;
        }

        String playerId = move.getPlayerId();
        int expectedPlayerNum = gameState.getCurrentPlayer();
        
        // Auto-claim the slot if it is empty!
        if (expectedPlayerNum == 1 && gameState.getPlayer1Id() == null) {
            if (playerId.equals(gameState.getPlayer2Id())) {
                gameState.setMessage("You are already Player 2!");
                return gameState;
            }
            gameState.setPlayer1Id(playerId);
        } else if (expectedPlayerNum == 2 && gameState.getPlayer2Id() == null) {
            if (playerId.equals(gameState.getPlayer1Id())) {
                gameState.setMessage("You are already Player 1!");
                return gameState;
            }
            gameState.setPlayer2Id(playerId);
        }

        String expectedPlayerId = expectedPlayerNum == 1 ? gameState.getPlayer1Id() : gameState.getPlayer2Id();

        if (expectedPlayerId == null || !expectedPlayerId.equals(playerId)) {
            gameState.setMessage("Not your turn, or you are a spectator!");
            return gameState;
        }

        int player = expectedPlayerNum;

        Integer col = move.getColumn();
        if (col == null || col < 0 || col > 6) {
            gameState.setMessage("Invalid column!");
            return gameState;
        }

        int[][] board = gameState.getBoard();
        int placedRow = -1;
        // Find the lowest empty spot (row 5 is bottom, row 0 is top)
        for (int r = 5; r >= 0; r--) {
            if (board[r][col] == 0) {
                board[r][col] = player;
                placedRow = r;
                break;
            }
        }

        if (placedRow == -1) {
            gameState.setMessage("Column is full!");
            return gameState;
        }

        gameState.setMessage(""); // clear previous messages

        if (checkWin(placedRow, col, player)) {
            gameState.setStatus(player == 1 ? GameState.Status.PLAYER_1_WON : GameState.Status.PLAYER_2_WON);
        } else if (isBoardFull()) {
            gameState.setStatus(GameState.Status.DRAW);
        } else {
            gameState.setCurrentPlayer(gameState.getCurrentPlayer() == 1 ? 2 : 1);
        }

        return gameState;
    }

    public synchronized GameState resetGame() {
        // We wipe the player assignments so that new players can join the new match.
        gameState = new GameState();
        return gameState;
    }

    private boolean checkWin(int r, int c, int player) {
        return checkDirection(r, c, player, 0, 1) || // Horizontal
               checkDirection(r, c, player, 1, 0) || // Vertical
               checkDirection(r, c, player, 1, 1) || // Diagonal down-right
               checkDirection(r, c, player, 1, -1);  // Diagonal down-left
    }

    private boolean checkDirection(int r, int c, int player, int dr, int dc) {
        int count = 1; // Current piece
        int[][] board = gameState.getBoard();

        // Check one way
        int r1 = r + dr;
        int c1 = c + dc;
        while (r1 >= 0 && r1 < 6 && c1 >= 0 && c1 < 7 && board[r1][c1] == player) {
            count++;
            r1 += dr;
            c1 += dc;
        }

        // Check the other way
        int r2 = r - dr;
        int c2 = c - dc;
        while (r2 >= 0 && r2 < 6 && c2 >= 0 && c2 < 7 && board[r2][c2] == player) {
            count++;
            r2 -= dr;
            c2 -= dc;
        }

        return count >= 4;
    }

    private boolean isBoardFull() {
        int[][] board = gameState.getBoard();
        for (int c = 0; c < 7; c++) {
            if (board[0][c] == 0) {
                return false;
            }
        }
        return true;
    }
}
