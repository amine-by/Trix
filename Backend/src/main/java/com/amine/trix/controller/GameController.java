package com.amine.trix.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amine.trix.dto.ConnectToGameDto;
import com.amine.trix.dto.CreateGameDto;
import com.amine.trix.dto.GameplayDto;
import com.amine.trix.dto.MoveDto;
import com.amine.trix.exception.GameNotFoundException;
import com.amine.trix.exception.InvalidGameException;
import com.amine.trix.exception.InvalidMoveException;
import com.amine.trix.exception.InvalidParamException;
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
	public ResponseEntity<GameplayDto> createGame(@RequestBody CreateGameDto createGameRequest) {
		log.info("create game request {}", createGameRequest);
		return ResponseEntity.ok(gameService.createGame(createGameRequest));
	}

	@PostMapping("/connect")
	public ResponseEntity<GameplayDto> connectToGame(@RequestBody ConnectToGameDto connectToGameRequest)
			throws InvalidParamException, InvalidGameException {
		log.info("connect to game request {}", connectToGameRequest);
		return ResponseEntity.ok(gameService.connectToGame(connectToGameRequest));
	}

	@PostMapping("/select")
	public ResponseEntity<GameplayDto> gameSelect(@RequestBody MoveDto moveDto)
			throws InvalidParamException, InvalidGameException, GameNotFoundException, InvalidMoveException {
		log.info("game selection request {}", moveDto);
		return ResponseEntity.ok(gameService.gameSelect(moveDto));
	}

	@PostMapping("/play")
	public ResponseEntity<GameplayDto> playCard(@RequestBody MoveDto moveDto)
			throws InvalidParamException, InvalidGameException, GameNotFoundException, InvalidMoveException {
		log.info("play a card request {}", moveDto);
		return ResponseEntity.ok(gameService.playCard(moveDto));
	}
}
