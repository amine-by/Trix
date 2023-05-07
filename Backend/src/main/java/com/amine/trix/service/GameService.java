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
	public Game createGame(CreateGameRequest createGameRequest) {
		Game game = new Game();
		game.setPlayers(new ArrayList<Player>());

		Player player = new Player();
		// Initializes the first player score, hand, and available games (kingdoms)
		player.initializePlayer(createGameRequest.getLogin());
		game.setGameId(UUID.randomUUID().toString());
		game.getPlayers().add(player);
		game.setGameOwner(0);
		game.setTurn(1);
		game.setStatus(GameStatus.NEW);
		GameStorage.getInstance().setGame(game);
		return game;
	}

	// Connects a player to the game and initializes them
	// Starts the game in case it finds the last player
	public Game connectToGame(ConnectToGameRequest connectToGameRequest)
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

		GameStorage.getInstance().setGame(game);
		return game;
	}

	// Enables the game owner to select a game (kingdom)
	public Game gameSelect(GameplayRequest gameplayRequest)
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
		} else
			game.setNormalBoard(new ArrayList<Card>());

		gameOwner.getAvailableGames().remove(gameplayRequest.getMove());
		game.setStatus(GameStatus.ROUND_IN_PROGRESS);
		return game;
	}

	public Game playCard(GameplayRequest gameplayRequest)
			throws GameNotFoundException, InvalidGameException, InvalidParamException, InvalidMoveException {
		if (!GameStorage.getInstance().getGames().containsKey(gameplayRequest.getGameId()))
			throw new GameNotFoundException("Game does not exist");

		Game game = GameStorage.getInstance().getGames().get(gameplayRequest.getGameId());

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
			if (game.getNormalBoard().size() != 0
					&& !playedCard.getSuit().equals(game.getNormalBoard().get(0).getSuit())
					&& player.suitNumberInHand(playedCard.getSuit()) > 0)
				throw new InvalidParamException("Selected card is not playable");

			if (game.getCurrentKingdom() == Kingdom.KING_OF_HEARTS && playedCard.getRank() == Rank.KING
					&& playedCard.getSuit() == Suit.HEART && player.getHand().size() == 8
					&& (game.getNormalBoard().size() == 0
							|| (game.boardContainsSuit(Suit.HEART) && player.suitNumberInHand(Suit.HEART) == 1)))
				throw new InvalidParamException("King of heart is not playable");

			if (game.getCurrentKingdom() == Kingdom.DIAMONDS && game.getNormalBoard().size() == 0
					&& playedCard.getSuit() == Suit.DIAMOND && game.collectedCardsContainsSuit(Suit.DIAMOND))
				throw new InvalidParamException("Diamond cards are not playable yet");

			game.playNormalCard(playedCard, game.getTurn());
			game.nextTurn();

			if (game.getNormalBoard().size() == 4) {
				final int RECIVING_PLAYER_INDEX = game.highestPlayerOnBoard();
				int addedScore;
				int collectedCardsFound;
				if (game.getCurrentKingdom() == Kingdom.KING_OF_HEARTS) {
					if (game.boardContainsCard(Suit.HEART, Rank.KING)) {
						addedScore = RECIVING_PLAYER_INDEX == game.getGameOwner() ? 200 : 100;
						game.getPlayers().get(RECIVING_PLAYER_INDEX)
								.setScore(game.getPlayers().get(RECIVING_PLAYER_INDEX).getScore() + addedScore);
						if (game.getPlayers().get(RECIVING_PLAYER_INDEX).getScore() % 1000 == 0)
							game.getPlayers().get(RECIVING_PLAYER_INDEX).setScore(0);
						game.checkEndOfTurnGameEnd();
					} else {
						game.setNormalBoard(new ArrayList<Card>());
						game.nextTurn();
					}
				}

				final int CAPOT = game.hasCapot();

				if (game.getCurrentKingdom() == Kingdom.QUEENS) {
					if (player.getHand().size() == 0) {
						if (CAPOT != -1) {
							addedScore = 160;
							for (int i = 0; i < 4; i++) {
								if (i != CAPOT) {
									if (i == game.getGameOwner())
										game.getPlayers().get(i)
												.setScore(game.getPlayers().get(i).getScore() + addedScore * 2);
									else
										game.getPlayers().get(i)
												.setScore(game.getPlayers().get(i).getScore() + addedScore);
									if (game.getPlayers().get(i).getScore() % 1000 == 0)
										game.getPlayers().get(i).setScore(0);
								}
							}
						} else {
							collectedCardsFound = 0;
							for (int i = 0; i < 4; i++) {
								addedScore = 0;
								for (Card card : game.getPlayers().get(i).getCollectedCards()) {
									if (card.getRank() == Rank.QUEEN) {
										addedScore += 20;
										collectedCardsFound++;
										if (addedScore == 80)
											addedScore *= 2;
									}
									if (collectedCardsFound == 4)
										break;
								}
								if (addedScore > 0) {
									if (i == game.getGameOwner())
										addedScore *= 2;
									game.getPlayers().get(i).setScore(game.getPlayers().get(i).getScore() + addedScore);
									if (game.getPlayers().get(i).getScore() % 1000 == 0)
										game.getPlayers().get(i).setScore(0);
								}
								if (collectedCardsFound == 4)
									break;
							}
						}
						game.checkEndOfTurnGameEnd();

					} else {
						collectedCardsFound = 0;
						for (int i = 0; i < 4; i++) {
							addedScore = 0;
							for (Card card : game.getPlayers().get(i).getCollectedCards()) {
								if (card.getRank() == Rank.QUEEN) {
									addedScore += 20;
									collectedCardsFound++;
									if (addedScore == 80)
										addedScore *= 2;
								}
								if (collectedCardsFound == 4)
									break;
							}
							if (addedScore > 0) {
								if (i == game.getGameOwner())
									addedScore *= 2;
								game.getPlayers().get(i).setScore(game.getPlayers().get(i).getScore() + addedScore);
								if (game.getPlayers().get(i).getScore() % 1000 == 0)
									game.getPlayers().get(i).setScore(0);
							}
							if (collectedCardsFound == 4)
								break;
						}
						if (collectedCardsFound == 4)
							game.checkEndOfTurnGameEnd();
						else {
							game.setNormalBoard(new ArrayList<Card>());
							game.nextTurn();
						}
					}
				}

				if (game.getCurrentKingdom() == Kingdom.DIAMONDS) {
					if (player.getHand().size() == 0) {
						if (CAPOT != -1) {
							addedScore = 80;
							for (int i = 0; i < 4; i++) {
								if (i != CAPOT) {
									if (i == game.getGameOwner())
										game.getPlayers().get(i)
												.setScore(game.getPlayers().get(i).getScore() + addedScore * 2);
									else
										game.getPlayers().get(i)
												.setScore(game.getPlayers().get(i).getScore() + addedScore);
									if (game.getPlayers().get(i).getScore() % 1000 == 0)
										game.getPlayers().get(i).setScore(0);
								}
							}

						} else {
							collectedCardsFound = 0;
							for (int i = 0; i < 4; i++) {
								addedScore = 0;
								for (Card card : game.getPlayers().get(i).getCollectedCards()) {
									if (card.getSuit() == Suit.DIAMOND) {
										addedScore += 10;
										collectedCardsFound++;
									}
									if (collectedCardsFound == 8)
										break;
								}
								if (addedScore > 0) {
									if (i == game.getGameOwner())
										addedScore *= 2;
									game.getPlayers().get(i).setScore(game.getPlayers().get(i).getScore() + addedScore);
									if (game.getPlayers().get(i).getScore() % 1000 == 0)
										game.getPlayers().get(i).setScore(0);
								}
								if (collectedCardsFound == 8)
									break;
							}
						}
						game.checkEndOfTurnGameEnd();
					} else {
						collectedCardsFound = 0;
						for (int i = 0; i < 4; i++) {
							addedScore = 0;
							for (Card card : game.getPlayers().get(i).getCollectedCards()) {
								if (card.getSuit() == Suit.DIAMOND) {
									addedScore += 10;
									collectedCardsFound++;
								}
								if (collectedCardsFound == 8)
									break;
							}
							if (addedScore > 0) {
								if (i == game.getGameOwner())
									addedScore *= 2;
								game.getPlayers().get(i).setScore(game.getPlayers().get(i).getScore() + addedScore);
								if (game.getPlayers().get(i).getScore() % 1000 == 0)
									game.getPlayers().get(i).setScore(0);
							}
							if (collectedCardsFound == 8)
								break;
						}
						if (collectedCardsFound == 8)
							game.checkEndOfTurnGameEnd();
						else {
							game.setNormalBoard(new ArrayList<Card>());
							game.nextTurn();
						}
					}

					if (game.getCurrentKingdom() == Kingdom.GENERAL) {
						if (player.getHand().size() == 0) {
							if (CAPOT != -1) {
								addedScore = 260;
								for (int i = 0; i < 4; i++) {
									if (i != CAPOT) {
										if (i == game.getGameOwner())
											game.getPlayers().get(i)
													.setScore(game.getPlayers().get(i).getScore() + addedScore * 2);
										else
											game.getPlayers().get(i)
													.setScore(game.getPlayers().get(i).getScore() + addedScore);
										if (game.getPlayers().get(i).getScore() % 1000 == 0)
											game.getPlayers().get(i).setScore(0);
									}
								}
							} else {
								collectedCardsFound = 0;
								for (int i = 0; i < 4; i++) {
									addedScore = 0;
									for (Card card : game.getPlayers().get(i).getCollectedCards()) {
										if (card.getSuit() == Suit.DIAMOND) {
											addedScore += 10;
											collectedCardsFound++;
										}
										if (card.getRank() == Rank.QUEEN) {
											addedScore += 20;
											collectedCardsFound++;
										}
										if (card.getRank() == Rank.KING && card.getSuit() == Suit.HEART) {
											addedScore += 100;
											collectedCardsFound++;
										}
										if (collectedCardsFound == 13)
											break;
									}
									if (addedScore > 0) {
										if (i == game.getGameOwner())
											addedScore *= 2;
										game.getPlayers().get(i)
												.setScore(game.getPlayers().get(i).getScore() + addedScore);
										if (game.getPlayers().get(i).getScore() % 1000 == 0)
											game.getPlayers().get(i).setScore(0);
									}
									if (collectedCardsFound == 13)
										break;
								}
							}
							game.checkEndOfTurnGameEnd();

						} else {
							collectedCardsFound = 0;
							for (int i = 0; i < 4; i++) {
								addedScore = 0;
								for (Card card : game.getPlayers().get(i).getCollectedCards()) {
									if (card.getSuit() == Suit.DIAMOND) {
										addedScore += 10;
										collectedCardsFound++;
									}
									if (card.getRank() == Rank.QUEEN) {
										addedScore += 20;
										collectedCardsFound++;
									}
									if (card.getRank() == Rank.KING && card.getSuit() == Suit.HEART) {
										addedScore += 100;
										collectedCardsFound++;
									}
									if (collectedCardsFound == 13)
										break;
								}
								if (addedScore > 0) {
									if (i == game.getGameOwner())
										addedScore *= 2;
									game.getPlayers().get(i).setScore(game.getPlayers().get(i).getScore() + addedScore);
									if (game.getPlayers().get(i).getScore() % 1000 == 0)
										game.getPlayers().get(i).setScore(0);
								}
								if (collectedCardsFound == 13)
									break;
							}
							if (collectedCardsFound == 13)
								game.checkEndOfTurnGameEnd();
							else {
								game.setNormalBoard(new ArrayList<Card>());
								game.nextTurn();
							}
						}
					}
				}
			}
		}

		return game;
	}
}
