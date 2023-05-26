package com.amine.trix.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amine.trix.exception.GameNotFoundException;
import com.amine.trix.exception.InvalidGameException;
import com.amine.trix.exception.InvalidMoveException;
import com.amine.trix.exception.InvalidParamException;
import com.amine.trix.payload.request.ConnectToGameRequest;
import com.amine.trix.payload.request.CreateGameRequest;
import com.amine.trix.payload.request.GameplayRequest;
import com.amine.trix.payload.response.GameplayResponse;
import com.amine.trix.service.GameService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/api/game")
public class GameController {
	private final GameService gameService;

	@PostMapping("/create")
	public ResponseEntity<GameplayResponse> createGame(@RequestBody CreateGameRequest createGameRequest) {
		log.info("create game request {}", createGameRequest);
		return ResponseEntity.ok(gameService.createGame(createGameRequest));
	}

	@PostMapping("/connect")
	public ResponseEntity<GameplayResponse> connectToGame(@RequestBody ConnectToGameRequest connectToGameRequest)
			throws InvalidParamException, InvalidGameException {
		log.info("connect to game request {}", connectToGameRequest);
		return ResponseEntity.ok(gameService.connectToGame(connectToGameRequest));
	}

	@PostMapping("/select")
	public ResponseEntity<GameplayResponse> gameSelect(@RequestBody GameplayRequest gameplayRequest)
			throws InvalidParamException, InvalidGameException, GameNotFoundException, InvalidMoveException {
		log.info("game selection request {}", gameplayRequest);
		return ResponseEntity.ok(gameService.gameSelect(gameplayRequest));
	}

	@PostMapping("/play")
	public ResponseEntity<GameplayResponse> playCard(@RequestBody GameplayRequest gameplayRequest)
			throws InvalidParamException, InvalidGameException, GameNotFoundException, InvalidMoveException {
		log.info("play a card request {}", gameplayRequest);
		return ResponseEntity.ok(gameService.playCard(gameplayRequest));
	}
}
