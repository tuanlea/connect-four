package com.peddex.demo.game;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class GameController {

    private final GameService gameService;

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @MessageMapping("/game.move")
    @SendTo("/topic/game.state")
    public GameState processMove(GameMove move) {
        return gameService.makeMove(move);
    }

    @MessageMapping("/game.reset")
    @SendTo("/topic/game.state")
    public GameState resetGame() {
        return gameService.resetGame();
    }

    @MessageMapping("/game.join")
    @SendTo("/topic/game.state")
    public GameState processJoin(GameMove joinMsg) {
        System.out.println("processJoin called with playerId: " + joinMsg.getPlayerId());
        return gameService.joinGame(joinMsg.getPlayerId());
    }
}
