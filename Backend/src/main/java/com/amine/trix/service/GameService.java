package com.amine.trix.service;

import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.amine.trix.dto.ConnectToGameDto;
import com.amine.trix.dto.CreateGameDto;
import com.amine.trix.dto.GameplayDto;
import com.amine.trix.dto.MoveDto;
import com.amine.trix.exception.GameNotFoundException;
import com.amine.trix.exception.InvalidGameException;
import com.amine.trix.exception.InvalidMoveException;
import com.amine.trix.exception.InvalidParamException;
import com.amine.trix.model.Card;
import com.amine.trix.model.Game;
import com.amine.trix.repository.GameRepository;

import lombok.AllArgsConstructor;

import com.amine.trix.model.GameStatus;
import com.amine.trix.model.Kingdom;
import com.amine.trix.model.Player;
import com.amine.trix.model.Rank;
import com.amine.trix.model.Suit;

@Service
@AllArgsConstructor
public class GameService {

	private final GameRepository gameRepository;
	private final SimpMessagingTemplate simpMessagingTemplate;

	// Creates a game and initializes the first player
	public GameplayDto createGame(CreateGameDto createGameRequest) {
		Game game = new Game();
		game.setPlayers(new ArrayList<Player>());

		Player player = new Player();
		// Initializes the first player score, hand, and available games (kingdoms)
		player.initializePlayer(createGameRequest.getPlayerId());
		game.getPlayers().add(player);
		game.setGameOwner(0);
		game.setTurn(1);
		game.setStatus(GameStatus.NEW);

		gameRepository.save(game);

		GameplayDto gameplayResponse = new GameplayDto();
		gameplayResponse.populateResponse(game, 0);
		return gameplayResponse;
	}

	// Connects a player to the game and initializes them
	// Starts the game in case it finds the last player
	public GameplayDto connectToGame(ConnectToGameDto connectToGameRequest)
			throws InvalidParamException, InvalidGameException {

		Game game = gameRepository.findById(connectToGameRequest.getGameId())
				.orElseThrow(() -> new InvalidParamException("Game does not exist"));

		if (game.getPlayers().size() == 4)
			throw new InvalidGameException("Game is full");

		Player player = new Player();
		// Initializes another player score, hand, and available games (kingdoms)
		player.initializePlayer(connectToGameRequest.getPlayerId());
		game.getPlayers().add(player);

		// Starts the game in case it finds the last player and distributes cards to
		// them
		if (game.getPlayers().size() == 4) {
			game.setStatus(GameStatus.KINGDOM_SELECTION);
			game.distributeCards();
		}

		gameRepository.save(game);

		GameplayDto gameplayResponse = new GameplayDto();
		gameplayResponse.populateResponse(game, game.getPlayers().size() - 1);
		return gameplayResponse;
	}

	// Enables the game owner to select a game (kingdom)
	public GameplayDto gameSelect(MoveDto moveDto)
			throws GameNotFoundException, InvalidGameException, InvalidParamException, InvalidMoveException {

		Game game = gameRepository.findById(moveDto.getGameId())
				.orElseThrow(() -> new InvalidParamException("Game does not exist"));

		if (!game.getStatus().equals(GameStatus.KINGDOM_SELECTION))
			throw new InvalidGameException("Game is not in game selection phase");

		Player gameOwner = game.getPlayers().get(game.getGameOwner());

		if (gameOwner == null)
			throw new InvalidParamException("Player does not exist");
		if (!gameOwner.getId().equals(moveDto.getPlayerId()))
			throw new InvalidMoveException("Player is not the game owner");
		if (gameOwner.getAvailableGames().get(moveDto.getMove()) == null)
			throw new InvalidParamException("Selected game is not available");

		game.setCurrentKingdom(gameOwner.getAvailableGames().get(moveDto.getMove()));

		if (game.getCurrentKingdom() == Kingdom.TRIX) {
			game.setTrixBoard(new boolean[32]);
			Arrays.fill(game.getTrixBoard(), false);
			game.determineTrixTurn();
		} else {
			if (game.getCurrentKingdom() != Kingdom.KING_OF_HEARTS)
				game.initPlayersCollectedCards();
			game.setNormalBoard(new ArrayList<Card>());
		}

		gameOwner.getAvailableGames().remove(moveDto.getMove());
		game.setStatus(GameStatus.ROUND_IN_PROGRESS);

		gameRepository.save(game);

		GameplayDto gameplayResponse = new GameplayDto();
		gameplayResponse.populateResponse(game, game.getGameOwner());
		return gameplayResponse;
	}

	public GameplayDto playCard(MoveDto moveDto)
			throws GameNotFoundException, InvalidGameException, InvalidParamException, InvalidMoveException {
		Game game = gameRepository.findById(moveDto.getGameId())
				.orElseThrow(() -> new InvalidParamException("Game does not exist"));

		final int TURN = game.getTurn();
		if (!game.getStatus().equals(GameStatus.ROUND_IN_PROGRESS))
			throw new InvalidGameException("Game is not in game selection phase");

		Player player = game.getPlayers().get(game.getTurn());

		if (player == null)
			throw new InvalidParamException("Player does not exist");

		if (!player.getId().equals(moveDto.getPlayerId()))
			throw new InvalidMoveException("Player is not on his turn");

		Card playedCard = player.getHand().get(moveDto.getMove());

		if (playedCard == null)
			throw new InvalidParamException("Selected card is not available");

		if (game.getCurrentKingdom() == Kingdom.TRIX)
			game.trixGameplay(moveDto.getPlayerId(), moveDto.getMove());
		else {
			if (game.getNormalBoard().size() != 0 && playedCard.getSuit() != game.getNormalBoard().get(0).getSuit()
					&& player.suitExistInHand(game.getNormalBoard().get(0).getSuit()))
				throw new InvalidParamException("Selected card is not playable");

			if (game.getCurrentKingdom() == Kingdom.KING_OF_HEARTS && playedCard.getRank() == Rank.KING
					&& playedCard.getSuit() == Suit.HEART && player.getHand().size() == 8
					&& (game.getNormalBoard().size() == 0
							|| (game.boardContainsSuit(Suit.HEART) && player.suitNumberInHand(Suit.HEART) == 1)))
				throw new InvalidParamException("King of heart is not playable");

			if (game.getCurrentKingdom() == Kingdom.DIAMONDS && game.getNormalBoard().size() == 0
					&& playedCard.getSuit() == Suit.DIAMOND && game.collectedCardsContainsSuit(Suit.DIAMOND))
				throw new InvalidParamException("Diamond cards are not playable yet");

			game.playNormalCard(playedCard, game.getTurn(), moveDto.getMove());

			if (game.getNormalBoard().size() == 4) {
				if (game.getCurrentKingdom() == Kingdom.KING_OF_HEARTS)
					game.endOfTurnKingOfHeartGameplay();
				else
					game.endOfTurnGameplay();
			} else
				game.nextTurn();
		}

		gameRepository.save(game);

		simpMessagingTemplate.convertAndSend("/play/topic/progress", game);

		GameplayDto gameplayResponse = new GameplayDto();
		gameplayResponse.populateResponse(game, TURN);
		return gameplayResponse;
	}
}
