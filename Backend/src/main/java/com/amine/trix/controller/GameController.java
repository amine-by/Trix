package com.amine.trix.controller;

import javax.security.auth.login.AccountNotFoundException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amine.trix.dto.ConnectToGameDto;
import com.amine.trix.dto.GameplayDto;
import com.amine.trix.dto.MoveDto;
import com.amine.trix.exception.GameNotFoundException;
import com.amine.trix.exception.InvalidGameException;
import com.amine.trix.exception.InvalidMoveException;
import com.amine.trix.exception.InvalidParamException;
import com.amine.trix.exception.UserAlreadyInGameException;
import com.amine.trix.service.GameService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/game")
public class GameController {
	private final GameService gameService;

	@PostMapping("/create")
	public ResponseEntity<GameplayDto> createGame() throws AccountNotFoundException, UserAlreadyInGameException {
		return ResponseEntity.ok(gameService.createGame());
	}

	@PostMapping("/connect")
	public ResponseEntity<GameplayDto> connectToGame(@RequestBody ConnectToGameDto connectToGameRequest)
			throws InvalidParamException, InvalidGameException, AccountNotFoundException, UserAlreadyInGameException {
		return ResponseEntity.ok(gameService.connectToGame(connectToGameRequest));
	}

	@PostMapping("/select")
	public ResponseEntity<GameplayDto> gameSelect(@RequestBody MoveDto moveRequest)
			throws InvalidParamException, InvalidGameException, GameNotFoundException, InvalidMoveException, AccountNotFoundException {
		return ResponseEntity.ok(gameService.gameSelect(moveRequest));
	}

	@PostMapping("/play")
	public ResponseEntity<GameplayDto> playCard(@RequestBody MoveDto moveRequest)
			throws InvalidParamException, InvalidGameException, GameNotFoundException, InvalidMoveException, AccountNotFoundException {
		return ResponseEntity.ok(gameService.playCard(moveRequest));
	}
}
