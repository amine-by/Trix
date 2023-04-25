package com.branper.trix.service;

import java.util.Arrays;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.branper.trix.exception.GameNotFoundException;
import com.branper.trix.exception.InvalidGameException;
import com.branper.trix.exception.InvalidMoveException;
import com.branper.trix.exception.InvalidParamException;
import com.branper.trix.model.Card;
import com.branper.trix.model.Game;
import com.branper.trix.model.GameStatus;
import com.branper.trix.model.Kingdom;
import com.branper.trix.model.GamePlay;
import com.branper.trix.model.Player;
import com.branper.trix.model.Rank;
import com.branper.trix.model.Suit;
import com.branper.trix.storage.GameStorage;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class GameService {
	public Game createGame(String login) {
		Game game = new Game();
		Player player = new Player();
		player.intializePlayer(login);
		game.setGameId(UUID.randomUUID().toString());
		game.getPlayers().add(player);
		game.setGameOwner(0);
		game.setTurn(1);
		game.setStatus(GameStatus.NEW);
		GameStorage.getInstance().setGame(game);
		return game;
	}

	public Game connectToGame(String login, String gameId) throws InvalidParamException, InvalidGameException {
		if (!GameStorage.getInstance().getGames().containsKey(gameId))
			throw new InvalidParamException("Game does not exist");

		Game game = GameStorage.getInstance().getGames().get(gameId);

		if (game.getPlayers().size() == 4)
			throw new InvalidGameException("Game is full");

		Player player = new Player();
		player.intializePlayer(login);
		game.getPlayers().add(player);
		if (game.getPlayers().size() == 4) {
			game.setStatus(GameStatus.KINGDOM_SELECTION);
			game.distributeCards();
		}
		GameStorage.getInstance().setGame(game);
		return game;
	}

	public Game gameSelect(GamePlay gamePlay)
			throws GameNotFoundException, InvalidGameException, InvalidParamException, InvalidMoveException {

		if (!GameStorage.getInstance().getGames().containsKey(gamePlay.getGameId()))
			throw new GameNotFoundException("Game does not exist");

		Game game = GameStorage.getInstance().getGames().get(gamePlay.getGameId());

		if (!game.getStatus().equals(GameStatus.KINGDOM_SELECTION))
			throw new InvalidGameException("Game is not in game selection phase");

		Player player = game.getPlayers().get(game.getGameOwner());

		if (player == null)
			throw new InvalidParamException("Player does not exist");
		if (player.getLogin() != gamePlay.getGameId())
			throw new InvalidMoveException("Player is not the game owner");
		if (player.getAvailableGames().get(gamePlay.getMove()) == null)
			throw new InvalidParamException("Selected game is not available");

		game.setCurrentKingdom(player.getAvailableGames().get(gamePlay.getMove()));

		if (game.getCurrentKingdom() == Kingdom.TRIX) {
			game.setBoard(new Boolean[32]);
			Arrays.fill(game.getBoard(), false);
			game.determineTrixTurn();
		} else
			game.setBoard(new Card[4]);

		player.getAvailableGames().remove(gamePlay.getMove());
		game.setStatus(GameStatus.ROUND_IN_PROGRESS);
		return game;
	}

	public Game playCard(GamePlay gamePlay)
			throws GameNotFoundException, InvalidGameException, InvalidParamException, InvalidMoveException {
		if (!GameStorage.getInstance().getGames().containsKey(gamePlay.getGameId()))
			throw new GameNotFoundException("Game does not exist");

		Game game = GameStorage.getInstance().getGames().get(gamePlay.getGameId());

		if (!game.getStatus().equals(GameStatus.ROUND_IN_PROGRESS))
			throw new InvalidGameException("Game is not in game selection phase");

		Player player = game.getPlayers().get(game.getTurn());

		if (player == null)
			throw new InvalidParamException("Player does not exist");
		if (player.getLogin() != gamePlay.getGameId())
			throw new InvalidMoveException("Player is not on his turn");

		Card playedCard = player.getHand().get(gamePlay.getMove());

		if (playedCard == null)
			throw new InvalidParamException("Selected card is not available");

		if (game.getCurrentKingdom() == Kingdom.TRIX) {
			int cardRankValue = playedCard.getRank().getValue();
			int cardSuitValue = playedCard.getSuit().getValue();
			if (cardRankValue == 4
					|| cardRankValue > 4 && (game.getBoard()[cardSuitValue * 8 + cardRankValue - 1] == "true")
					|| cardRankValue < 4 && (game.getBoard()[cardSuitValue * 8 + cardRankValue + 1] == "true")) {

				game.playTrixCard(cardSuitValue * 8 + cardRankValue);
				player.getHand().remove(gamePlay.getMove());

				if (player.getHand().size() == 0) {
					int addedScore;
					if (game.remainingPlayersNumber() == 3) {
						addedScore = -100;
						if (player.getLogin() == gamePlay.getLogin())
							addedScore *= 2;
						player.setScore(addedScore);
						game.determineTrixTurn();
					} else if (game.remainingPlayersNumber() == 2) {
						addedScore = -50;
						if (player.getLogin() == gamePlay.getLogin())
							addedScore *= 2;
						player.setScore(player.getScore() + addedScore);
						if (game.isGameEnded())
							game.setStatus(GameStatus.FINISHED);
						else {
							game.distributeCards();
							game.nextGameOwnerAndInitTurn();
							game.setStatus(GameStatus.KINGDOM_SELECTION);
						}
					}
				} else if (cardRankValue != 7)
					game.determineTrixTurn();

			} else {
				throw new InvalidParamException("Selected card is not playable");
			}
		} else {
			final int TURN_IN_CYCLE = Math.abs(game.getTurn() - game.getGameOwner());
			Card[] normalBoard = (Card[]) game.getBoard();
			if (TURN_IN_CYCLE != 0 && playedCard.getSuit() != normalBoard[0].getSuit()
					&& player.suitNumberInHand(playedCard.getSuit()) > 0)
				throw new InvalidParamException("Selected card is not playable");

			if (game.getCurrentKingdom() == Kingdom.KING_OF_HEARTS && playedCard.getRank() == Rank.KING
					&& playedCard.getSuit() == Suit.HEART && player.getHand().size() == 8 && (TURN_IN_CYCLE == 0
							|| (game.boardContainsSuit(Suit.HEART) && player.suitNumberInHand(Suit.HEART) == 1)))
				throw new InvalidParamException("King of heart is not playable");

			if (game.getCurrentKingdom() == Kingdom.DIAMONDS && TURN_IN_CYCLE == 0
					&& playedCard.getSuit() == Suit.DIAMOND && game.collectedCardsContainsSuit(Suit.DIAMOND))
				throw new InvalidParamException("Diamond cards are not playable yet");

			game.playNormalCard(playedCard, game.getTurn());

			if (TURN_IN_CYCLE == 3) {
				int addedScore;
				switch (game.getCurrentKingdom()) {
				case KING_OF_HEARTS:
					final int KING_OF_HEART_INDEX = game.boardContainsCardAt(Suit.HEART, Rank.KING);
					if (KING_OF_HEART_INDEX != -1) {
						addedScore = game.getTurn() == game.getGameOwner() ? 200 : 100;
						game.getPlayers().get(KING_OF_HEART_INDEX)
								.setScore(game.getPlayers().get(KING_OF_HEART_INDEX).getScore() + addedScore);
						if (game.isGameEnded())
							game.setStatus(GameStatus.FINISHED);
						else {
							game.distributeCards();
							game.nextGameOwnerAndInitTurn();
							game.setStatus(GameStatus.KINGDOM_SELECTION);
						}
					} else {
						game.setBoard(new Card[4]);
						game.nextTurn();
					}
					break;
				case QUEENS:
					break;
				case DIAMONDS:
					break;
				default:
					break;
				}
			}
		}

		return game;
	}
}
