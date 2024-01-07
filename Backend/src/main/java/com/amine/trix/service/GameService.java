package com.amine.trix.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.security.auth.login.AccountNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.amine.trix.dto.AvailableGamesDto;
import com.amine.trix.dto.GameplayDto;
import com.amine.trix.dto.JoinGameDto;
import com.amine.trix.dto.MoveDto;
import com.amine.trix.enums.GameStatus;
import com.amine.trix.enums.Kingdom;
import com.amine.trix.enums.Rank;
import com.amine.trix.enums.Suit;
import com.amine.trix.exception.InvalidGameException;
import com.amine.trix.exception.InvalidMoveException;
import com.amine.trix.exception.InvalidParamException;
import com.amine.trix.exception.UserAlreadyInGameException;
import com.amine.trix.exception.UserIsNotInGameException;
import com.amine.trix.model.Card;
import com.amine.trix.model.Game;
import com.amine.trix.repository.GameRepository;
import com.amine.trix.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import com.amine.trix.model.Player;
import com.amine.trix.model.User;

@Service
@RequiredArgsConstructor
public class GameService {

	private final GameRepository gameRepository;
	private final UserRepository userRepository;
	private final SimpMessagingTemplate simpMessagingTemplate;

	// Returns list of games that are not full
	public AvailableGamesDto findAvailableGames() {
		AvailableGamesDto availableGamesResponse = new AvailableGamesDto();
		// TODO: Add GamePageDto for a more customized request?
		Page<Game> games = gameRepository.findAvailableGames(PageRequest.of(0, 5));
		availableGamesResponse.setGames(new ArrayList<>());
		games.forEach((game) -> availableGamesResponse.getGames().add(game.getId()));
		return availableGamesResponse;
	}

	// Checks if player is already in a game
	public boolean isPlayerInGame() throws AccountNotFoundException {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new AccountNotFoundException("User does not exist"));
		return user.getCurrentGame() != null;
	}

	// Creates a game and initializes the first player
	public boolean createGame() throws AccountNotFoundException, UserAlreadyInGameException {

		String email = SecurityContextHolder.getContext().getAuthentication().getName();

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new AccountNotFoundException("User does not exist"));

		if (user.getCurrentGame() != null)
			throw new UserAlreadyInGameException("User already in another game");

		Game game = new Game();
		game.setPlayers(new ArrayList<Player>());

		Player player = new Player();
		// Initializes the first player score, hand, and available games (kingdoms)
		initializePlayer(player, email, user.getName());
		game.getPlayers().add(player);
		game.setGameOwner(0);
		game.setTurn(1);
		game.setStatus(GameStatus.NEW);

		gameRepository.save(game);

		user.setCurrentGame(game.getId());
		userRepository.save(user);

		return true;
	}

	// Connects a player to the game and initializes them
	// Starts the game in case it finds the last player
	public boolean joinGame(JoinGameDto joinGameRequest)
			throws InvalidParamException, InvalidGameException, AccountNotFoundException, UserAlreadyInGameException {

		String email = SecurityContextHolder.getContext().getAuthentication().getName();

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new AccountNotFoundException("User does not exist"));

		if (user.getCurrentGame() != null)
			throw new UserAlreadyInGameException("User already in a game");

		Game game = gameRepository.findById(joinGameRequest.getGameId())
				.orElseThrow(() -> new InvalidParamException("Game does not exist"));

		if (user.getCurrentGame() == game.getId())
			throw new UserAlreadyInGameException("User already in this game");

		if (game.getPlayers().size() == 4)
			throw new InvalidGameException("Game is full");

		Player player = new Player();
		// Initializes another player score, hand, and available games (kingdoms)
		initializePlayer(player, email, user.getName());
		game.getPlayers().add(player);

		// Starts the game in case it finds the last player and distributes cards to
		// them
		if (game.getPlayers().size() == 4) {
			game.setStatus(GameStatus.KINGDOM_SELECTION);
			distributeCards(game);
		}

		gameRepository.save(game);
		user.setCurrentGame(game.getId());
		userRepository.save(user);

		updatePlayers(game, game.getPlayers().size() - 1);

		return true;
	}

	public GameplayDto connectToGame()
			throws AccountNotFoundException, UserIsNotInGameException, InvalidParamException {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new AccountNotFoundException("User does not exist"));

		if (user.getCurrentGame() == null)
			throw new UserIsNotInGameException("User is not in a game");

		Game game = gameRepository.findById(user.getCurrentGame())
				.orElseThrow(() -> new InvalidParamException("Game does not exist"));

		GameplayDto gameplayResponse = new GameplayDto();
		gameplayResponse.populateResponse(game, determinePlayerIndexByEmail(game, email));
		return gameplayResponse;
	}

	// Enables the game owner to select a game (kingdom)
	public GameplayDto gameSelect(MoveDto moveResponse)
			throws InvalidGameException, InvalidParamException, InvalidMoveException, AccountNotFoundException {

		String email = SecurityContextHolder.getContext().getAuthentication().getName();

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new AccountNotFoundException("User does not exist"));

		Game game = gameRepository.findById(moveResponse.getGameId())
				.orElseThrow(() -> new InvalidParamException("Game does not exist"));

		if (!game.getId().equals(user.getCurrentGame()))
			throw new InvalidGameException("User is not in this game");

		if (!game.getStatus().equals(GameStatus.KINGDOM_SELECTION))
			throw new InvalidMoveException("Game is not in game selection phase");

		Player gameOwner = game.getPlayers().get(game.getGameOwner());

		if (gameOwner == null)
			throw new InvalidParamException("Player does not exist");
		if (!gameOwner.getId().equals(email))
			throw new InvalidMoveException("Player is not the game owner");
		if (gameOwner.getAvailableGames().get(moveResponse.getMove()) == null)
			throw new InvalidParamException("Selected game is not available");

		game.setCurrentKingdom(gameOwner.getAvailableGames().get(moveResponse.getMove()));

		if (game.getCurrentKingdom() == Kingdom.TRIX) {
			game.setTrixBoard(new boolean[32]);
			Arrays.fill(game.getTrixBoard(), false);
			determineTrixTurn(game);
		} else {
			if (game.getCurrentKingdom() != Kingdom.KING_OF_HEARTS)
				initPlayersCollectedCards(game);
			game.setNormalBoard(new ArrayList<Card>());
		}

		gameOwner.getAvailableGames().remove(moveResponse.getMove());
		game.setStatus(GameStatus.ROUND_IN_PROGRESS);

		gameRepository.save(game);

		GameplayDto gameplayResponse = new GameplayDto();
		gameplayResponse.populateResponse(game, game.getGameOwner());

		updatePlayers(game, game.getGameOwner());

		return gameplayResponse;
	}

	public GameplayDto playCard(MoveDto moveResponse) throws InvalidGameException,

			InvalidParamException, InvalidMoveException, AccountNotFoundException, InterruptedException {

		String email = SecurityContextHolder.getContext().getAuthentication().getName();

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new AccountNotFoundException("User does not exist"));

		Game game = gameRepository.findById(moveResponse.getGameId())
				.orElseThrow(() -> new InvalidParamException("Game does not exist"));

		if (!game.getId().equals(user.getCurrentGame()))
			throw new InvalidGameException("User is not in this game");

		final int TURN = game.getTurn();
		if (!game.getStatus().equals(GameStatus.ROUND_IN_PROGRESS))
			throw new InvalidGameException("Game is not in game selection phase");

		Player player = game.getPlayers().get(game.getTurn());

		if (player == null)
			throw new InvalidParamException("Player does not exist");

		if (!player.getId().equals(email))
			throw new InvalidMoveException("Player is not on his turn");

		Card playedCard = player.getHand().get(moveResponse.getMove());

		if (playedCard == null)
			throw new InvalidParamException("Selected card is not available");

		if (game.getCurrentKingdom() == Kingdom.TRIX)
			trixGameplay(game, user, moveResponse.getMove());
		else {
			if (game.getNormalBoard().size() != 0 && playedCard.getSuit() != game.getNormalBoard().get(0).getSuit()
					&& suitExistInHand(player, game.getNormalBoard().get(0).getSuit()))
				throw new InvalidParamException("Selected card is not playable");

			if (game.getCurrentKingdom() == Kingdom.KING_OF_HEARTS && playedCard.getRank() == Rank.KING
					&& playedCard.getSuit() == Suit.HEART && player.getHand().size() == 8
					&& (game.getNormalBoard().size() == 0
							|| (boardContainsSuit(game, Suit.HEART) && suitNumberInHand(player, Suit.HEART) == 1)))
				throw new InvalidParamException("King of heart is not playable");

			if (game.getCurrentKingdom() == Kingdom.DIAMONDS && game.getNormalBoard().size() == 0
					&& playedCard.getSuit() == Suit.DIAMOND && collectedCardsNotContainsSuit(game, Suit.DIAMOND))
				throw new InvalidParamException("Diamond cards are not playable yet");

			playNormalCard(game, playedCard, game.getTurn(), moveResponse.getMove());

			if (game.getNormalBoard().size() == 4) {
				updateAllPlayersWithDelay(game);
				if (game.getCurrentKingdom() == Kingdom.KING_OF_HEARTS)
					endOfTurnKingOfHeartGameplay(game, user);
				else
					endOfTurnGameplay(game, user);
			} else
				nextTurn(game);
		}

		gameRepository.save(game);
		userRepository.save(user);

		GameplayDto gameplayResponse = new GameplayDto();
		gameplayResponse.populateResponse(game, TURN);

		updatePlayers(game, TURN);

		return gameplayResponse;
	}

	private int determinePlayerIndexByEmail(Game game, String email) {
		for (int i = 1; i < game.getPlayers().size(); i++) {
			if (email.equals(game.getPlayers().get(i).getId()))
				return i;
		}
		return 0;
	}

	private void distributeCards(Game game) {
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

		game.getPlayers().get(0).setHand(new ArrayList<Card>(Arrays.asList(deck.get(0), deck.get(1), deck.get(2),
				deck.get(3), deck.get(4), deck.get(5), deck.get(6), deck.get(7))));
		game.getPlayers().get(1).setHand(new ArrayList<Card>(Arrays.asList(deck.get(8), deck.get(9), deck.get(10),
				deck.get(11), deck.get(12), deck.get(13), deck.get(14), deck.get(15))));
		game.getPlayers().get(2).setHand(new ArrayList<Card>(Arrays.asList(deck.get(16), deck.get(17), deck.get(18),
				deck.get(19), deck.get(20), deck.get(21), deck.get(22), deck.get(23))));
		game.getPlayers().get(3).setHand(new ArrayList<Card>(Arrays.asList(deck.get(24), deck.get(25), deck.get(26),
				deck.get(27), deck.get(28), deck.get(29), deck.get(30), deck.get(31))));

		for (Player player : game.getPlayers())
			player.getHand().sort((card1, card2) -> {
				int suitCompare = card1.getSuit().getValue().compareTo(card2.getSuit().getValue());
				if (suitCompare == 0)
					return card1.getRank().getNormalValue().compareTo(card2.getRank().getNormalValue());
				return suitCompare;
			});
	}

	private void nextTurn(Game game) {
		game.setTurn((game.getTurn() < 3) ? game.getTurn() + 1 : 0);
	}

	private void nextGameOwnerAndInitTurn(Game game) {
		game.setGameOwner((game.getGameOwner() < 3) ? game.getGameOwner() + 1 : 0);
		game.setTurn(game.getGameOwner() < 3 ? game.getGameOwner() + 1 : 0);
	}

	private void initPlayersCollectedCards(Game game) {
		for (Player player : game.getPlayers())
			player.setCollectedCards(new ArrayList<Card>());
	}

	private void determineTrixTurn(Game game) {
		ArrayList<Card> playerHand = game.getPlayers().get(game.getTurn()).getHand();
		for (Card card : playerHand) {
			int cardRankValue = card.getRank().getTrixValue();
			int cardSuitValue = card.getSuit().getValue();
			if (cardRankValue == 4
					|| cardRankValue > 4 && (boolean) game.getTrixBoard()[cardSuitValue * 8 + cardRankValue - 1]
					|| cardRankValue < 4 && (boolean) game.getTrixBoard()[cardSuitValue * 8 + cardRankValue + 1])
				return;
		}
		nextTurn(game);
		determineTrixTurn(game);
	}

	private void playTrixCard(Game game, int position) {
		game.getTrixBoard()[position] = true;
	}

	private void playNormalCard(Game game, Card card, int turn, int move) {
		game.getPlayers().get(turn).getHand().remove(move);
		game.getNormalBoard().add(card);
	}

	private int remainingPlayersNumber(Game game) {
		int result = 0;
		for (Player player : game.getPlayers())
			if (player.getHand().size() > 0)
				result++;
		return result;
	}

	private boolean boardContainsSuit(Game game, Suit suit) {
		for (Card card : game.getNormalBoard())
			if (card.getSuit() == suit)
				return true;
		return false;
	}

	private boolean boardContainsCard(Game game, Suit suit, Rank rank) {
		for (Card card : game.getNormalBoard()) {
			if (card != null && card.getSuit() == suit && card.getRank() == rank)
				return true;
		}
		return false;
	}

	private boolean collectedCardsNotContainsSuit(Game game, Suit suit) {
		for (Player player : game.getPlayers())
			for (Card card : player.getCollectedCards())
				if (card != null && card.getSuit() == suit)
					return false;
		return true;
	}

	private void checkEndOfTurnGameEnd(Game game, User user) {
		for (Player player : game.getPlayers())
			if (player.getAvailableGames().size() > 0) {
				distributeCards(game);
				nextGameOwnerAndInitTurn(game);
				game.setStatus(GameStatus.KINGDOM_SELECTION);
				return;
			}
		user.setCurrentGame(null);
		game.setStatus(GameStatus.FINISHED);
	}

	private int highestPlayerOnBoard(Game game) {
		int result = 0;
		for (int i = 1; i < game.getNormalBoard().size(); i++) {
			if (game.getNormalBoard().get(i).getSuit() == game.getNormalBoard().get(0).getSuit()
					&& game.getNormalBoard().get(i).getRank().getNormalValue() > game.getNormalBoard().get(result)
							.getRank().getNormalValue())
				result = i;
		}
		return (result + game.getTurn() + 1) % 4;
	}

	private int hasCapot(Game game) {
		for (int i = 0; i < 4; i++)
			if (game.getPlayers().get(i).getCollectedCards().size() == 32)
				return i;
		return -1;
	}

	private boolean noCapot(Game game) {
		int numberOfPlayersWhoCollectedCards = 0;
		for (Player player : game.getPlayers()) {
			if (player.getCollectedCards().size() > 0)
				numberOfPlayersWhoCollectedCards++;
			if (numberOfPlayersWhoCollectedCards > 1)
				return true;
		}
		return false;
	}

	private void trixGameplay(Game game, User user, int move) throws InvalidParamException {
		int cardRankValue = game.getPlayers().get(game.getTurn()).getHand().get(move).getRank().getTrixValue();
		int cardSuitValue = game.getPlayers().get(game.getTurn()).getHand().get(move).getSuit().getValue();
		Player player = game.getPlayers().get(game.getTurn());
		if (cardRankValue == 4 || cardRankValue > 4 && game.getTrixBoard()[cardSuitValue * 8 + cardRankValue - 1]
				|| cardRankValue < 4 && game.getTrixBoard()[cardSuitValue * 8 + cardRankValue + 1]) {

			playTrixCard(game, cardSuitValue * 8 + cardRankValue);
			player.getHand().remove(move);

			if (player.getHand().size() == 0) {
				int addedScore;
				if (remainingPlayersNumber(game) == 3) {
					addedScore = -100;
					if (player.getId() == user.getId())
						addedScore *= 2;
					player.setScore(player.getScore() + addedScore);
					if (player.getScore() % 1000 == 0)
						player.setScore(0);
					determineTrixTurn(game);
				} else if (remainingPlayersNumber(game) == 2) {
					addedScore = -50;
					if (player.getId() == user.getId())
						addedScore *= 2;
					player.setScore(player.getScore() + addedScore);
					if (player.getScore() % 1000 == 0)
						player.setScore(0);
					checkEndOfTurnGameEnd(game, user);
				}
			} else {
				if (cardRankValue != 7)
					nextTurn(game);
				determineTrixTurn(game);
			}

		} else {
			throw new InvalidParamException("Selected card is not playable");
		}
	}

	private void endOfTurnKingOfHeartGameplay(Game game, User user) {
		final int RECIVING_PLAYER_INDEX = highestPlayerOnBoard(game);
		Player recivingPlayer = game.getPlayers().get(RECIVING_PLAYER_INDEX);
		if (boardContainsCard(game, Suit.HEART, Rank.KING)) {
			int addedScore = RECIVING_PLAYER_INDEX == game.getGameOwner() ? 200 : 100;
			recivingPlayer.setScore(recivingPlayer.getScore() + addedScore);
			if (recivingPlayer.getScore() % 1000 == 0)
				recivingPlayer.setScore(0);
			checkEndOfTurnGameEnd(game, user);
		} else
			game.setTurn(RECIVING_PLAYER_INDEX);
		game.setNormalBoard(new ArrayList<Card>());
	}

	private void endOfTurnGameplay(Game game, User user) {
		final int RECIVING_PLAYER_INDEX = highestPlayerOnBoard(game);
		Player player = game.getPlayers().get(game.getTurn());
		Player currentPlayer;
		game.getPlayers().get(RECIVING_PLAYER_INDEX).getCollectedCards().addAll(game.getNormalBoard());
		game.setNormalBoard(new ArrayList<Card>());
		final int CAPOT = hasCapot(game);
		int collectedCardsFound;
		int addedScore;
		if (player.getHand().size() == 0) {
			if (CAPOT != -1) {
				switch (game.getCurrentKingdom()) {
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
						currentPlayer = game.getPlayers().get(i);
						if (i == game.getGameOwner())
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
					currentPlayer = game.getPlayers().get(i);
					for (Card card : currentPlayer.getCollectedCards()) {
						if (game.getCurrentKingdom() == Kingdom.QUEENS) {
							if (card.getRank() == Rank.QUEEN) {
								addedScore += 20;
								collectedCardsFound++;
								if (addedScore == 80)
									addedScore *= 2;
							}
							if (collectedCardsFound == 4)
								break;
						} else if (game.getCurrentKingdom() == Kingdom.DIAMONDS) {
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
						if (i == game.getGameOwner())
							addedScore *= 2;
						currentPlayer.setScore(currentPlayer.getScore() + addedScore);
						if (currentPlayer.getScore() % 1000 == 0)
							currentPlayer.setScore(0);
					}
					if (game.getCurrentKingdom() == Kingdom.QUEENS && collectedCardsFound == 4
							|| game.getCurrentKingdom() == Kingdom.DIAMONDS && collectedCardsFound == 8
							|| collectedCardsFound == 13)
						break;
				}
			}
			checkEndOfTurnGameEnd(game, user);

		} else if (noCapot(game)) {
			int[] addedScoreTable = { 0, 0, 0, 0 };
			collectedCardsFound = 0;
			for (int i = 0; i < 4; i++) {
				currentPlayer = game.getPlayers().get(i);
				for (Card card : currentPlayer.getCollectedCards()) {
					if (game.getCurrentKingdom() == Kingdom.QUEENS) {
						if (card.getRank() == Rank.QUEEN) {
							addedScoreTable[i] += 20;
							collectedCardsFound++;
							if (addedScoreTable[i] == 80)
								addedScoreTable[i] *= 2;
						}
						if (collectedCardsFound == 4)
							break;
					} else if (game.getCurrentKingdom() == Kingdom.DIAMONDS) {
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

			if (game.getCurrentKingdom() == Kingdom.QUEENS && collectedCardsFound == 4
					|| game.getCurrentKingdom() == Kingdom.DIAMONDS && collectedCardsFound == 8
					|| collectedCardsFound == 13) {
				for (int j = 0; j < addedScoreTable.length; j++) {
					currentPlayer = game.getPlayers().get(j);
					if (addedScoreTable[j] > 0) {
						if (j == game.getGameOwner())
							addedScoreTable[j] *= 2;
						currentPlayer.setScore(currentPlayer.getScore() + addedScoreTable[j]);
						if (currentPlayer.getScore() % 1000 == 0)
							currentPlayer.setScore(0);
					}
				}
				checkEndOfTurnGameEnd(game, user);
			}
		} else
			game.setTurn(RECIVING_PLAYER_INDEX);
	}

	private void initializePlayer(Player player, String email, String name) {
		player.setId(email);
		player.setName(name);
		player.setScore(0);
		player.setAvailableGames(new ArrayList<Kingdom>(Arrays.asList(Kingdom.KING_OF_HEARTS, Kingdom.QUEENS,
				Kingdom.DIAMONDS, Kingdom.GENERAL, Kingdom.TRIX)));
	}

	private boolean suitExistInHand(Player player, Suit suit) {
		for (Card card : player.getHand())
			if (card.getSuit() == suit)
				return true;
		return false;
	}

	private int suitNumberInHand(Player player, Suit suit) {
		int result = 0;
		for (Card card : player.getHand())
			if (card.getSuit() == suit)
				result++;
		return result;
	}

	private void updatePlayers(Game game, int currentPlayer) {
		// TODO: should update this algorithm to make it less slow
		for (int i = 0; i < game.getPlayers().size(); i++) {
			if (i != currentPlayer) {
				GameplayDto gameplayResponse = new GameplayDto();
				gameplayResponse.populateResponse(game, i);
				simpMessagingTemplate.convertAndSendToUser(game.getPlayers().get(i).getId(), "/queue",
						gameplayResponse);
			}
		}
	}

	private void updateAllPlayersWithDelay(Game game) throws InterruptedException {
		for (int i = 0; i < game.getPlayers().size(); i++) {
			GameplayDto gameplayResponse = new GameplayDto();
			gameplayResponse.populateResponse(game, i);
			simpMessagingTemplate.convertAndSendToUser(game.getPlayers().get(i).getId(), "/queue", gameplayResponse);
		}
		Thread.sleep(1000);
	}
}
