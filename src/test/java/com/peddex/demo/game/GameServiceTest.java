package com.peddex.demo.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {

    private GameService gameService;

    @BeforeEach
    public void setUp() {
        gameService = new GameService();
    }

    @Test
    public void testInitialState() {
        GameState state = gameService.getGameState();
        assertEquals(GameState.Status.IN_PROGRESS, state.getStatus());
        assertEquals(1, state.getCurrentPlayer());
    }

    @Test
    public void testValidMove() {
        GameState state = gameService.makeMove(new GameMove(1, 0));
        assertEquals(1, state.getBoard()[5][0]);
        assertEquals(2, state.getCurrentPlayer());
    }

    @Test
    public void testHorizontalWin() {
        gameService.makeMove(new GameMove(1, 0));
        gameService.makeMove(new GameMove(2, 0));
        gameService.makeMove(new GameMove(1, 1));
        gameService.makeMove(new GameMove(2, 1));
        gameService.makeMove(new GameMove(1, 2));
        gameService.makeMove(new GameMove(2, 2));
        
        GameState state = gameService.makeMove(new GameMove(1, 3));
        assertEquals(GameState.Status.PLAYER_1_WON, state.getStatus());
    }

    @Test
    public void testVerticalWin() {
        gameService.makeMove(new GameMove(1, 0));
        gameService.makeMove(new GameMove(2, 1));
        gameService.makeMove(new GameMove(1, 0));
        gameService.makeMove(new GameMove(2, 1));
        gameService.makeMove(new GameMove(1, 0));
        gameService.makeMove(new GameMove(2, 1));
        
        GameState state = gameService.makeMove(new GameMove(1, 0));
        assertEquals(GameState.Status.PLAYER_1_WON, state.getStatus());
    }

    @Test
    public void testDiagonalWinRightDown() {
        gameService.makeMove(new GameMove(1, 0)); // 1
        gameService.makeMove(new GameMove(2, 1)); // 2
        gameService.makeMove(new GameMove(1, 1)); // 1
        gameService.makeMove(new GameMove(2, 2)); // 2
        gameService.makeMove(new GameMove(1, 2)); // 1
        gameService.makeMove(new GameMove(2, 3)); // 2
        gameService.makeMove(new GameMove(1, 2)); // 1
        gameService.makeMove(new GameMove(2, 3)); // 2
        gameService.makeMove(new GameMove(1, 4)); // 1
        gameService.makeMove(new GameMove(2, 3)); // 2
        GameState state = gameService.makeMove(new GameMove(1, 3)); // 1 -> Win!
        
        assertEquals(GameState.Status.PLAYER_1_WON, state.getStatus());
    }

    @Test
    public void testDiagonalWinLeftDown() {
        gameService.makeMove(new GameMove(1, 3)); // 1
        gameService.makeMove(new GameMove(2, 2)); // 2
        gameService.makeMove(new GameMove(1, 2)); // 1
        gameService.makeMove(new GameMove(2, 1)); // 2
        gameService.makeMove(new GameMove(1, 1)); // 1
        gameService.makeMove(new GameMove(2, 0)); // 2
        gameService.makeMove(new GameMove(1, 1)); // 1
        gameService.makeMove(new GameMove(2, 0)); // 2
        gameService.makeMove(new GameMove(1, 4)); // 1
        gameService.makeMove(new GameMove(2, 0)); // 2
        GameState state = gameService.makeMove(new GameMove(1, 0)); // 1 -> Win!
        
        assertEquals(GameState.Status.PLAYER_1_WON, state.getStatus());
    }

    @Test
    public void testColumnFull() {
        for (int i = 0; i < 6; i++) {
            gameService.makeMove(new GameMove(i % 2 == 0 ? 1 : 2, 0));
        }
        GameState state = gameService.makeMove(new GameMove(1, 0));
        assertEquals("Column is full!", state.getMessage());
    }

    @Test
    public void testWrongTurn() {
        GameState state = gameService.makeMove(new GameMove(2, 0));
        assertEquals("Not your turn!", state.getMessage());
    }
}
