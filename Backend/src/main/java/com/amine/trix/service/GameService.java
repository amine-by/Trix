package com.amine.trix.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.amine.trix.exception.GameNotFoundException;
import com.amine.trix.exception.InvalidGameException;
import com.amine.trix.exception.InvalidMoveException;
import com.amine.trix.exception.InvalidParamException;
import com.amine.trix.model.Card;
import com.amine.trix.model.Game;
import com.amine.trix.payload.request.ConnectToGameRequest;
import com.amine.trix.payload.request.CreateGameRequest;
import com.amine.trix.payload.request.GameplayRequest;
import com.amine.trix.payload.response.GameplayResponse;
import com.amine.trix.model.GameStatus;
import com.amine.trix.model.Kingdom;
import com.amine.trix.model.Player;
import com.amine.trix.model.Rank;
import com.amine.trix.model.Suit;
import com.amine.trix.storage.GameStorage;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class GameService {
	// Creates a game and initializes the first player
	public GameplayResponse createGame(CreateGameRequest createGameRequest) {
		Game game = new Game();
		game.setPlayers(new ArrayList<Player>());

		String gameId = UUID.randomUUID().toString();

		Player player = new Player();
		// Initializes the first player score, hand, and available games (kingdoms)
		player.initializePlayer(createGameRequest.getLogin());
		game.setGameId(gameId);
		game.getPlayers().add(player);
		game.setGameOwner(0);
		game.setTurn(1);
		game.setStatus(GameStatus.NEW);
		GameStorage.getInstance().setGame(game);

		GameplayResponse gameplayResponse = new GameplayResponse();
		return gameplayResponse.populateResponse(gameId, 0);
	}

	// Connects a player to the game and initializes them
	// Starts the game in case it finds the last player
	public GameplayResponse connectToGame(ConnectToGameRequest connectToGameRequest)
			throws InvalidParamException, InvalidGameException {
		if (!GameStorage.getInstance().getGames().containsKey(connectToGameRequest.getGameId()))
			throw new InvalidParamException("Game does not exist");

		Game game = GameStorage.getInstance().getGames().get(connectToGameRequest.getGameId());

		if (game.getPlayers().size() == 4)
			throw new InvalidGameException("Game is full");

		Player player = new Player();
		// Initializes another player score, hand, and available games (kingdoms)
		player.initializePlayer(connectToGameRequest.getLogin());
		game.getPlayers().add(player);

		// Starts the game in case it finds the last player and distributes cards to
		// them
		if (game.getPlayers().size() == 4) {
			game.setStatus(GameStatus.KINGDOM_SELECTION);
			game.distributeCards();
		}

		GameplayResponse gameplayResponse = new GameplayResponse();
		gameplayResponse.populateResponse(game.getGameId(), game.getPlayers().size() - 1);
		return gameplayResponse.populateResponse(game.getGameId(), game.getPlayers().size() - 1);
	}

	// Enables the game owner to select a game (kingdom)
	public GameplayResponse gameSelect(GameplayRequest gameplayRequest)
			throws GameNotFoundException, InvalidGameException, InvalidParamException, InvalidMoveException {

		if (!GameStorage.getInstance().getGames().containsKey(gameplayRequest.getGameId()))
			throw new GameNotFoundException("Game does not exist");

		Game game = GameStorage.getInstance().getGames().get(gameplayRequest.getGameId());

		if (!game.getStatus().equals(GameStatus.KINGDOM_SELECTION))
			throw new InvalidGameException("Game is not in game selection phase");

		Player gameOwner = game.getPlayers().get(game.getGameOwner());

		if (gameOwner == null)
			throw new InvalidParamException("Player does not exist");
		if (!gameOwner.getLogin().equals(gameplayRequest.getLogin()))
			throw new InvalidMoveException("Player is not the game owner");
		if (gameOwner.getAvailableGames().get(gameplayRequest.getMove()) == null)
			throw new InvalidParamException("Selected game is not available");

		game.setCurrentKingdom(gameOwner.getAvailableGames().get(gameplayRequest.getMove()));

		if (game.getCurrentKingdom() == Kingdom.TRIX) {
			game.setTrixBoard(new boolean[32]);
			Arrays.fill(game.getTrixBoard(), false);
			game.determineTrixTurn();
		} else {
			if (game.getCurrentKingdom() != Kingdom.KING_OF_HEARTS)
				game.initPlayersCollectedCards();
			game.setNormalBoard(new ArrayList<Card>());
		}

		gameOwner.getAvailableGames().remove(gameplayRequest.getMove());
		game.setStatus(GameStatus.ROUND_IN_PROGRESS);
		GameplayResponse gameplayResponse = new GameplayResponse();
		return gameplayResponse.populateResponse(game.getGameId(), game.getGameOwner());
	}

	public GameplayResponse playCard(GameplayRequest gameplayRequest)
			throws GameNotFoundException, InvalidGameException, InvalidParamException, InvalidMoveException {
		if (!GameStorage.getInstance().getGames().containsKey(gameplayRequest.getGameId()))
			throw new GameNotFoundException("Game does not exist");

		Game game = GameStorage.getInstance().getGames().get(gameplayRequest.getGameId());

		final int TURN = game.getTurn();
		if (!game.getStatus().equals(GameStatus.ROUND_IN_PROGRESS))
			throw new InvalidGameException("Game is not in game selection phase");

		Player player = game.getPlayers().get(game.getTurn());

		if (player == null)
			throw new InvalidParamException("Player does not exist");

		if (!player.getLogin().equals(gameplayRequest.getLogin()))
			throw new InvalidMoveException("Player is not on his turn");

		Card playedCard = player.getHand().get(gameplayRequest.getMove());

		if (playedCard == null)
			throw new InvalidParamException("Selected card is not available");

		if (game.getCurrentKingdom() == Kingdom.TRIX)
			game.trixGameplay(gameplayRequest.getLogin(), gameplayRequest.getMove());
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

			game.playNormalCard(playedCard, game.getTurn(), gameplayRequest.getMove());

			if (game.getNormalBoard().size() == 4) {
				if (game.getCurrentKingdom() == Kingdom.KING_OF_HEARTS)
					game.endOfTurnKingOfHeartGameplay();
				else
					game.endOfTurnGameplay();
			} else
				game.nextTurn();
		}

		GameplayResponse gameplayResponse = new GameplayResponse();

		return gameplayResponse.populateResponse(game.getGameId(), TURN);
	}
}
