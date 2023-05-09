package com.amine.trix.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import com.amine.trix.exception.InvalidParamException;

import lombok.Data;

@Data
public class Game {
	private String gameId;
	private ArrayList<Player> players;
	private int gameOwner;
	private int turn;
	private boolean[] trixBoard;
	private ArrayList<Card> normalBoard;
	private GameStatus status;
	private Kingdom currentKingdom;

	public void distributeCards() {
		ArrayList<Card> deck = new ArrayList<Card>(Arrays.asList(new Card(Rank.ACE, Suit.HEART),
				new Card(Rank.SEVEN, Suit.HEART), new Card(Rank.EIGHT, Suit.HEART), new Card(Rank.NINE, Suit.HEART),
				new Card(Rank.TEN, Suit.HEART), new Card(Rank.JACK, Suit.HEART), new Card(Rank.QUEEN, Suit.HEART),
				new Card(Rank.KING, Suit.HEART), new Card(Rank.ACE, Suit.SPADE), new Card(Rank.SEVEN, Suit.SPADE),
				new Card(Rank.EIGHT, Suit.SPADE), new Card(Rank.NINE, Suit.SPADE), new Card(Rank.TEN, Suit.SPADE),
				new Card(Rank.JACK, Suit.SPADE), new Card(Rank.QUEEN, Suit.SPADE), new Card(Rank.KING, Suit.SPADE),
				new Card(Rank.ACE, Suit.CLUB), new Card(Rank.SEVEN, Suit.CLUB), new Card(Rank.EIGHT, Suit.CLUB),
				new Card(Rank.NINE, Suit.CLUB), new Card(Rank.TEN, Suit.CLUB), new Card(Rank.JACK, Suit.CLUB),
				new Card(Rank.QUEEN, Suit.CLUB), new Card(Rank.KING, Suit.CLUB), new Card(Rank.ACE, Suit.DIAMOND),
				new Card(Rank.SEVEN, Suit.DIAMOND), new Card(Rank.EIGHT, Suit.DIAMOND),
				new Card(Rank.NINE, Suit.DIAMOND), new Card(Rank.TEN, Suit.DIAMOND), new Card(Rank.JACK, Suit.DIAMOND),
				new Card(Rank.QUEEN, Suit.DIAMOND), new Card(Rank.KING, Suit.DIAMOND)));

		Collections.shuffle(deck);

		players.get(0).setHand(new ArrayList<Card>(Arrays.asList(deck.get(0), deck.get(1), deck.get(2), deck.get(3),
				deck.get(4), deck.get(5), deck.get(6), deck.get(7))));
		players.get(1).setHand(new ArrayList<Card>(Arrays.asList(deck.get(8), deck.get(9), deck.get(10), deck.get(11),
				deck.get(12), deck.get(13), deck.get(14), deck.get(15))));
		players.get(2).setHand(new ArrayList<Card>(Arrays.asList(deck.get(16), deck.get(17), deck.get(18), deck.get(19),
				deck.get(20), deck.get(21), deck.get(22), deck.get(23))));
		players.get(3).setHand(new ArrayList<Card>(Arrays.asList(deck.get(24), deck.get(25), deck.get(26), deck.get(27),
				deck.get(28), deck.get(29), deck.get(30), deck.get(31))));

		for (Player player : players)
			player.getHand().sort((card1, card2) -> {
				int suitCompare = card1.getSuit().getValue().compareTo(card2.getSuit().getValue());
				if (suitCompare == 0)
					return card1.getRank().getNormalValue().compareTo(card2.getRank().getNormalValue());
				return suitCompare;
			});
	}

	public void nextTurn() {
		turn = (turn < 3) ? turn + 1 : 0;
	}

	public void nextGameOwnerAndInitTurn() {
		gameOwner = (gameOwner < 3) ? gameOwner + 1 : 0;
		turn = gameOwner < 3 ? gameOwner + 1 : 0;
	}

	public void initPlayersCollectedCards() {
		for (Player player : players)
			player.setCollectedCards(new ArrayList<Card>());
	}

	public void determineTrixTurn() {
		ArrayList<Card> playerHand = players.get(turn).getHand();
		for (Card card : playerHand) {
			int cardRankValue = card.getRank().getTrixValue();
			int cardSuitValue = card.getSuit().getValue();
			if (cardRankValue == 4 || cardRankValue > 4 && (boolean) trixBoard[cardSuitValue * 8 + cardRankValue - 1]
					|| cardRankValue < 4 && (boolean) trixBoard[cardSuitValue * 8 + cardRankValue + 1])
				return;
		}
		nextTurn();
		determineTrixTurn();
	}

	public void playTrixCard(int position) {
		trixBoard[position] = true;
	}

	public void playNormalCard(Card card, int turn, int move) {
		players.get(turn).getHand().remove(move);
		normalBoard.add(card);
	}

	public int remainingPlayersNumber() {
		int result = 0;
		for (Player player : players)
			if (player.getHand().size() > 0)
				result++;
		return result;
	}

	public boolean boardContainsSuit(Suit suit) {
		for (Card card : normalBoard)
			if (card.getSuit() == suit)
				return true;
		return false;
	}

	public boolean boardContainsCard(Suit suit, Rank rank) {
		for (Card card : normalBoard) {
			if (card != null && card.getSuit() == suit && card.getRank() == rank)
				return true;
		}
		return false;
	}

	public boolean collectedCardsContainsSuit(Suit suit) {
		for (Player player : players)
			for (Card card : player.getCollectedCards())
				if (card != null && card.getSuit() == suit)
					return true;
		return false;
	}

	public void checkEndOfTurnGameEnd() {
		for (Player player : players)
			if (player.getAvailableGames().size() > 0) {
				distributeCards();
				nextGameOwnerAndInitTurn();
				status = GameStatus.KINGDOM_SELECTION;
				return;
			}
		status = GameStatus.FINISHED;
	}

	public int highestPlayerOnBoard() {
		int result = 0;
		for (int i = 1; i < normalBoard.size(); i++) {
			if (normalBoard.get(i).getSuit() == normalBoard.get(0).getSuit() && normalBoard.get(i).getRank()
					.getNormalValue() > normalBoard.get(result).getRank().getNormalValue())
				result = i;
		}
		return (result + turn + 1) % 4;
	}

	public int hasCapot() {
		for (int i = 0; i < 4; i++)
			if (players.get(i).getCollectedCards().size() == 32)
				return i;
		return -1;
	}

	public boolean noCapot() {
		int numberOfPlayersWhoCollectedCards = 0;
		for (Player player : players) {
			if (player.getCollectedCards().size() > 0)
				numberOfPlayersWhoCollectedCards++;
			if (numberOfPlayersWhoCollectedCards > 1)
				return true;
		}
		return false;
	}

	public void trixGameplay(String login, int move) throws InvalidParamException {
		int cardRankValue = players.get(turn).getHand().get(move).getRank().getTrixValue();
		int cardSuitValue = players.get(turn).getHand().get(move).getSuit().getValue();
		Player player = players.get(turn);
		if (cardRankValue == 4 || cardRankValue > 4 && trixBoard[cardSuitValue * 8 + cardRankValue - 1]
				|| cardRankValue < 4 && trixBoard[cardSuitValue * 8 + cardRankValue + 1]) {

			playTrixCard(cardSuitValue * 8 + cardRankValue);
			player.getHand().remove(move);

			if (player.getHand().size() == 0) {
				int addedScore;
				if (remainingPlayersNumber() == 3) {
					addedScore = -100;
					if (player.getLogin() == login)
						addedScore *= 2;
					player.setScore(addedScore);
					if (player.getScore() % 1000 == 0)
						player.setScore(0);
					determineTrixTurn();
				} else if (remainingPlayersNumber() == 2) {
					addedScore = -50;
					if (player.getLogin() == login)
						addedScore *= 2;
					player.setScore(player.getScore() + addedScore);
					if (player.getScore() % 1000 == 0)
						player.setScore(0);
					checkEndOfTurnGameEnd();
				}
			} else {
				if (cardRankValue != 7)
					nextTurn();
				determineTrixTurn();
			}

		} else {
			throw new InvalidParamException("Selected card is not playable");
		}
	}

	public void endOfTurnKingOfHeartGameplay() {
		final int RECIVING_PLAYER_INDEX = highestPlayerOnBoard();
		Player recivingPlayer = players.get(RECIVING_PLAYER_INDEX);
		if (boardContainsCard(Suit.HEART, Rank.KING)) {
			int addedScore = RECIVING_PLAYER_INDEX == gameOwner ? 200 : 100;
			recivingPlayer.setScore(recivingPlayer.getScore() + addedScore);
			if (recivingPlayer.getScore() % 1000 == 0)
				recivingPlayer.setScore(0);
			checkEndOfTurnGameEnd();
		} else
			turn = RECIVING_PLAYER_INDEX;
		normalBoard = new ArrayList<Card>();
	}

	public void endOfTurnGameplay() {
		final int CAPOT = hasCapot();
		final int RECIVING_PLAYER_INDEX = highestPlayerOnBoard();
		int collectedCardsFound;
		int addedScore;
		Player player = players.get(turn);
		Player currentPlayer;
		players.get(RECIVING_PLAYER_INDEX).getCollectedCards().addAll(normalBoard);
		normalBoard = new ArrayList<Card>();
		if (player.getHand().size() == 0) {
			if (CAPOT != -1) {
				switch (currentKingdom) {
				case QUEENS:
					addedScore = 160;
					break;
				case DIAMONDS:
					addedScore = 80;
					break;
				default:
					addedScore = 260;
					break;
				}
				for (int i = 0; i < 4; i++) {
					if (i != CAPOT) {
						currentPlayer = players.get(i);
						if (i == getGameOwner())
							currentPlayer.setScore(currentPlayer.getScore() + addedScore * 2);
						else
							currentPlayer.setScore(currentPlayer.getScore() + addedScore);
						if (currentPlayer.getScore() % 1000 == 0)
							currentPlayer.setScore(0);
					}
				}
			} else {
				collectedCardsFound = 0;
				for (int i = 0; i < 4; i++) {
					addedScore = 0;
					currentPlayer = getPlayers().get(i);
					for (Card card : currentPlayer.getCollectedCards()) {
						if (currentKingdom == Kingdom.QUEENS) {
							if (card.getRank() == Rank.QUEEN) {
								addedScore += 20;
								collectedCardsFound++;
								if (addedScore == 80)
									addedScore *= 2;
							}
							if (collectedCardsFound == 4)
								break;
						} else if (currentKingdom == Kingdom.DIAMONDS) {
							if (card.getSuit() == Suit.DIAMOND) {
								addedScore += 10;
								collectedCardsFound++;
							}
							if (collectedCardsFound == 8)
								break;
						} else {
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
					}
					if (addedScore > 0) {
						if (i == gameOwner)
							addedScore *= 2;
						currentPlayer.setScore(currentPlayer.getScore() + addedScore);
						if (currentPlayer.getScore() % 1000 == 0)
							currentPlayer.setScore(0);
					}
					if (currentKingdom == Kingdom.QUEENS && collectedCardsFound == 4
							|| currentKingdom == Kingdom.DIAMONDS && collectedCardsFound == 8
							|| collectedCardsFound == 13)
						break;
				}
			}
			checkEndOfTurnGameEnd();

		} else if (noCapot()) {
			int[] addedScoreTable = { 0, 0, 0, 0 };
			collectedCardsFound = 0;
			for (int i = 0; i < 4; i++) {
				currentPlayer = getPlayers().get(i);
				for (Card card : currentPlayer.getCollectedCards()) {
					if (currentKingdom == Kingdom.QUEENS) {
						if (card.getRank() == Rank.QUEEN) {
							addedScoreTable[i] += 20;
							collectedCardsFound++;
							if (addedScoreTable[i] == 80)
								addedScoreTable[i] *= 2;
						}
						if (collectedCardsFound == 4)
							break;
					} else if (currentKingdom == Kingdom.DIAMONDS) {
						if (card.getSuit() == Suit.DIAMOND) {
							addedScoreTable[i] += 10;
							collectedCardsFound++;
						}
						if (collectedCardsFound == 8)
							break;
					} else {
						if (card.getSuit() == Suit.DIAMOND) {
							addedScoreTable[i] += 10;
							collectedCardsFound++;
						}
						if (card.getRank() == Rank.QUEEN) {
							addedScoreTable[i] += 20;
							collectedCardsFound++;
						}
						if (card.getRank() == Rank.KING && card.getSuit() == Suit.HEART) {
							addedScoreTable[i] += 100;
							collectedCardsFound++;
						}
						if (collectedCardsFound == 13)
							break;
					}
				}
			}

			if (currentKingdom == Kingdom.QUEENS && collectedCardsFound == 4
					|| currentKingdom == Kingdom.DIAMONDS && collectedCardsFound == 8 || collectedCardsFound == 13) {
				for (int j = 0; j < addedScoreTable.length; j++) {
					currentPlayer = players.get(j);
					if (addedScoreTable[j] > 0) {
						if (j == gameOwner)
							addedScoreTable[j] *= 2;
						currentPlayer.setScore(currentPlayer.getScore() + addedScoreTable[j]);
						if (currentPlayer.getScore() % 1000 == 0)
							currentPlayer.setScore(0);
					}
				}
				checkEndOfTurnGameEnd();
			}
		} else
			turn = RECIVING_PLAYER_INDEX;
	}
	
}
