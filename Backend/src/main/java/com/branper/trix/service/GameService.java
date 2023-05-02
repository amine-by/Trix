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
	// Creates a game and initializes the first player
	public Game createGame(String login) {
		Game game = new Game();
		Player player = new Player();
		// Initializes the first player score, hand, and available games (kingdoms)
		player.initializePlayer(login);
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
	public Game connectToGame(String login, String gameId) throws InvalidParamException, InvalidGameException {
		if (!GameStorage.getInstance().getGames().containsKey(gameId))
			throw new InvalidParamException("Game does not exist");

		Game game = GameStorage.getInstance().getGames().get(gameId);

		if (game.getPlayers().size() == 4)
			throw new InvalidGameException("Game is full");

		Player player = new Player();
		// Initializes another player score, hand, and available games (kingdoms)
		player.initializePlayer(login);
		game.getPlayers().add(player);

		// Starts the game in case it finds the last player and distributes cards to the
		// players
		if (game.getPlayers().size() == 4) {
			game.setStatus(GameStatus.KINGDOM_SELECTION);
			game.distributeCards();
		}
		GameStorage.getInstance().setGame(game);
		return game;
	}

	// Enables the game owner to select a game (kingdom)
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
			int cardRankValue = playedCard.getRank().getTrixValue();
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
						if (player.getScore() % 1000 == 0)
							player.setScore(0);
						game.determineTrixTurn();
					} else if (game.remainingPlayersNumber() == 2) {
						addedScore = -50;
						if (player.getLogin() == gamePlay.getLogin())
							addedScore *= 2;
						player.setScore(player.getScore() + addedScore);
						if (player.getScore() % 1000 == 0)
							player.setScore(0);
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
				final int RECIVING_PLAYER_INDEX = game.highestPlayerOnBoard();
				final int CAPOT = game.hasCapot();
				int addedScore;
				int collectedCardsFound;
				if (game.getCurrentKingdom() == Kingdom.KING_OF_HEARTS) {
					if (game.boardContainsCard(Suit.HEART, Rank.KING)) {
						addedScore = RECIVING_PLAYER_INDEX == game.getGameOwner() ? 200 : 100;
						game.getPlayers().get(RECIVING_PLAYER_INDEX)
								.setScore(game.getPlayers().get(RECIVING_PLAYER_INDEX).getScore() + addedScore);
						if (game.getPlayers().get(RECIVING_PLAYER_INDEX).getScore() % 1000 == 0)
							game.getPlayers().get(RECIVING_PLAYER_INDEX).setScore(0);
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
				}

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
						if (game.isGameEnded())
							game.setStatus(GameStatus.FINISHED);
						else {
							game.distributeCards();
							game.nextGameOwnerAndInitTurn();
							game.setStatus(GameStatus.KINGDOM_SELECTION);
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
						if (collectedCardsFound == 4)
							if (game.isGameEnded())
								game.setStatus(GameStatus.FINISHED);
							else {
								game.distributeCards();
								game.nextGameOwnerAndInitTurn();
								game.setStatus(GameStatus.KINGDOM_SELECTION);
							}
						else {
							game.setBoard(new Card[4]);
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

						if (game.isGameEnded())
							game.setStatus(GameStatus.FINISHED);
						else {
							game.distributeCards();
							game.nextGameOwnerAndInitTurn();
							game.setStatus(GameStatus.KINGDOM_SELECTION);
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
						if (collectedCardsFound == 8)
							if (game.isGameEnded())
								game.setStatus(GameStatus.FINISHED);
							else {
								game.distributeCards();
								game.nextGameOwnerAndInitTurn();
								game.setStatus(GameStatus.KINGDOM_SELECTION);
							}
						else {
							game.setBoard(new Card[4]);
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

							if (game.isGameEnded())
								game.setStatus(GameStatus.FINISHED);
							else {
								game.distributeCards();
								game.nextGameOwnerAndInitTurn();
								game.setStatus(GameStatus.KINGDOM_SELECTION);
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
									game.getPlayers().get(i).setScore(game.getPlayers().get(i).getScore() + addedScore);
									if (game.getPlayers().get(i).getScore() % 1000 == 0)
										game.getPlayers().get(i).setScore(0);
								}
								if (collectedCardsFound == 13)
									break;
							}
							if (collectedCardsFound == 13)
								if (game.isGameEnded())
									game.setStatus(GameStatus.FINISHED);
								else {
									game.distributeCards();
									game.nextGameOwnerAndInitTurn();
									game.setStatus(GameStatus.KINGDOM_SELECTION);
								}
							else {
								game.setBoard(new Card[4]);
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
