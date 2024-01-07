package com.amine.trix.controller;

import javax.security.auth.login.AccountNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amine.trix.dto.AvailableGamesDto;
import com.amine.trix.dto.JoinGameDto;
import com.amine.trix.dto.GameplayDto;
import com.amine.trix.dto.MoveDto;
import com.amine.trix.exception.InvalidGameException;
import com.amine.trix.exception.InvalidMoveException;
import com.amine.trix.exception.InvalidParamException;
import com.amine.trix.exception.UserAlreadyInGameException;
import com.amine.trix.exception.UserIsNotInGameException;
import com.amine.trix.service.GameService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/game")
public class GameController {
	private final GameService gameService;

	@GetMapping("/available")
	public ResponseEntity<AvailableGamesDto> findAvailableGames() {
		return new ResponseEntity<AvailableGamesDto>(gameService.findAvailableGames(), HttpStatus.OK);
	}

	@PostMapping("/check")
	public ResponseEntity<Boolean> isPlayerInGame() throws AccountNotFoundException {
		return new ResponseEntity<Boolean>(gameService.isPlayerInGame(), HttpStatus.OK);
	}

	@PostMapping("/create")
	public ResponseEntity<Boolean> createGame() throws AccountNotFoundException, UserAlreadyInGameException {
		return new ResponseEntity<Boolean>(gameService.createGame(), HttpStatus.CREATED);
	}

	@PostMapping("/join")
	public ResponseEntity<Boolean> joinGame(@RequestBody JoinGameDto joinGameRequest)
			throws InvalidParamException, InvalidGameException, AccountNotFoundException, UserAlreadyInGameException {
		return new ResponseEntity<Boolean>(gameService.joinGame(joinGameRequest), HttpStatus.OK);
	}

	@PostMapping("/connect")
	public ResponseEntity<GameplayDto> connectToGame()
			throws AccountNotFoundException, UserIsNotInGameException, InvalidParamException {
		return new ResponseEntity<GameplayDto>(gameService.connectToGame(), HttpStatus.OK);
	}

	@PostMapping("/select")
	public ResponseEntity<GameplayDto> gameSelect(@RequestBody MoveDto moveRequest) throws InvalidParamException,
			InvalidGameException, InvalidMoveException, AccountNotFoundException {
		return new ResponseEntity<GameplayDto>(gameService.gameSelect(moveRequest), HttpStatus.OK);
	}

	@PostMapping("/play")
	public ResponseEntity<GameplayDto> playCard(@RequestBody MoveDto moveRequest)
			throws InvalidParamException, InvalidGameException, InvalidMoveException,
			AccountNotFoundException, InterruptedException {
		return new ResponseEntity<GameplayDto>(gameService.playCard(moveRequest), HttpStatus.OK);
	}
}
