package com.amine.trix.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.amine.trix.exception.GameNotFoundException;
import com.amine.trix.exception.InvalidGameException;
import com.amine.trix.exception.InvalidMoveException;
import com.amine.trix.exception.InvalidParamException;
import com.amine.trix.model.Card;
import com.amine.trix.model.Game;
import com.amine.trix.model.Player;
import com.amine.trix.model.Rank;
import com.amine.trix.model.Suit;
import com.amine.trix.payload.request.ConnectToGameRequest;
import com.amine.trix.payload.request.CreateGameRequest;
import com.amine.trix.payload.request.GameplayRequest;
import com.amine.trix.payload.response.GameplayResponse;
import com.amine.trix.storage.GameStorage;

public class GameServiceTests {
	private final GameService gameService = new GameService();

	@Test
	void kingOfHeartsWinTest()
			throws InvalidParamException, InvalidGameException, GameNotFoundException, InvalidMoveException {

		GameplayResponse gameplayResponse = gameService.createGame(new CreateGameRequest("p1"));

		String gameId = gameplayResponse.getGameId();
		Game storedGame = GameStorage.getInstance().getGames().get(gameId);

		gameService.connectToGame(new ConnectToGameRequest("p2", gameId));
		gameService.connectToGame(new ConnectToGameRequest("p3", gameId));
		gameService.connectToGame(new ConnectToGameRequest("p4", gameId));
		gameService.gameSelect(new GameplayRequest(0, "p1", gameId));

		Player player1 = storedGame.getPlayers().get(0);
		Player player2 = storedGame.getPlayers().get(1);
		Player player3 = storedGame.getPlayers().get(2);
		Player player4 = storedGame.getPlayers().get(3);

		player1.setHand(new ArrayList<Card>());
		player1.getHand().add(new Card(Rank.KING, Suit.HEART));
		player2.setHand(new ArrayList<Card>());
		player2.getHand().add(new Card(Rank.SEVEN, Suit.SPADE));
		player3.setHand(new ArrayList<Card>());
		player3.getHand().add(new Card(Rank.EIGHT, Suit.SPADE));
		player4.setHand(new ArrayList<Card>());
		player4.getHand().add(new Card(Rank.ACE, Suit.HEART));

		gameService.playCard(new GameplayRequest(0, "p2", gameId));
		gameService.playCard(new GameplayRequest(0, "p3", gameId));
		gameService.playCard(new GameplayRequest(0, "p4", gameId));
		gameService.playCard(new GameplayRequest(0, "p1", gameId));

		assertThat(player1.getScore()).isEqualTo(0);
		assertThat(player2.getScore()).isEqualTo(0);
		assertThat(player3.getScore()).isEqualTo(100);
		assertThat(player4.getScore()).isEqualTo(0);
	}

	@Test
	void kingOfHeartsTurnTest()
			throws InvalidParamException, InvalidGameException, GameNotFoundException, InvalidMoveException {

		GameplayResponse gameplayResponse = gameService.createGame(new CreateGameRequest("p1"));

		String gameId = gameplayResponse.getGameId();
		Game storedGame = GameStorage.getInstance().getGames().get(gameId);

		gameService.connectToGame(new ConnectToGameRequest("p2", gameId));
		gameService.connectToGame(new ConnectToGameRequest("p3", gameId));
		gameService.connectToGame(new ConnectToGameRequest("p4", gameId));
		gameService.gameSelect(new GameplayRequest(0, "p1", gameId));

		Player player1 = storedGame.getPlayers().get(0);
		Player player2 = storedGame.getPlayers().get(1);
		Player player3 = storedGame.getPlayers().get(2);
		Player player4 = storedGame.getPlayers().get(3);

		player1.setHand(new ArrayList<Card>());
		player1.getHand().add(new Card(Rank.QUEEN, Suit.CLUB));
		player1.getHand().add(new Card(Rank.KING, Suit.HEART));
		player2.setHand(new ArrayList<Card>());
		player2.getHand().add(new Card(Rank.SEVEN, Suit.SPADE));
		player2.getHand().add(new Card(Rank.KING, Suit.CLUB));
		player3.setHand(new ArrayList<Card>());
		player3.getHand().add(new Card(Rank.EIGHT, Suit.SPADE));
		player3.getHand().add(new Card(Rank.JACK, Suit.DIAMOND));
		player4.setHand(new ArrayList<Card>());
		player4.getHand().add(new Card(Rank.ACE, Suit.HEART));
		player4.getHand().add(new Card(Rank.ACE, Suit.CLUB));

		gameService.playCard(new GameplayRequest(0, "p2", gameId));
		gameService.playCard(new GameplayRequest(0, "p3", gameId));
		gameService.playCard(new GameplayRequest(0, "p4", gameId));
		gameService.playCard(new GameplayRequest(0, "p1", gameId));

		assertThat(player1.getScore()).isEqualTo(0);
		assertThat(player2.getScore()).isEqualTo(0);
		assertThat(player3.getScore()).isEqualTo(0);
		assertThat(player4.getScore()).isEqualTo(0);
		assertThat(gameplayResponse.getTurn()).isEqualTo(2);
	}

	@Test
	void queensWinTest()
			throws InvalidParamException, InvalidGameException, GameNotFoundException, InvalidMoveException {
		GameplayResponse gameplayResponse = gameService.createGame(new CreateGameRequest("p1"));

		String gameId = gameplayResponse.getGameId();
		Game storedGame = GameStorage.getInstance().getGames().get(gameId);

		gameService.connectToGame(new ConnectToGameRequest("p2", gameId));
		gameService.connectToGame(new ConnectToGameRequest("p3", gameId));
		gameService.connectToGame(new ConnectToGameRequest("p4", gameId));
		gameService.gameSelect(new GameplayRequest(1, "p1", gameId));

		Player player1 = storedGame.getPlayers().get(0);
		Player player2 = storedGame.getPlayers().get(1);
		Player player3 = storedGame.getPlayers().get(2);
		Player player4 = storedGame.getPlayers().get(3);

		player2.setCollectedCards(new ArrayList<Card>(Arrays.asList(new Card(Rank.ACE, Suit.HEART),
				new Card(Rank.SEVEN, Suit.HEART), new Card(Rank.EIGHT, Suit.HEART), new Card(Rank.NINE, Suit.HEART),
				new Card(Rank.TEN, Suit.HEART), new Card(Rank.JACK, Suit.HEART), new Card(Rank.KING, Suit.HEART),
				new Card(Rank.ACE, Suit.SPADE), new Card(Rank.SEVEN, Suit.SPADE), new Card(Rank.EIGHT, Suit.SPADE),
				new Card(Rank.NINE, Suit.SPADE), new Card(Rank.TEN, Suit.SPADE), new Card(Rank.JACK, Suit.SPADE),
				new Card(Rank.KING, Suit.SPADE), new Card(Rank.ACE, Suit.CLUB), new Card(Rank.SEVEN, Suit.CLUB),
				new Card(Rank.EIGHT, Suit.CLUB), new Card(Rank.NINE, Suit.CLUB), new Card(Rank.TEN, Suit.CLUB),
				new Card(Rank.JACK, Suit.CLUB), new Card(Rank.KING, Suit.CLUB), new Card(Rank.ACE, Suit.DIAMOND),
				new Card(Rank.SEVEN, Suit.DIAMOND), new Card(Rank.EIGHT, Suit.DIAMOND),
				new Card(Rank.NINE, Suit.DIAMOND), new Card(Rank.TEN, Suit.DIAMOND), new Card(Rank.JACK, Suit.DIAMOND),
				new Card(Rank.KING, Suit.DIAMOND))));

		player1.setHand(new ArrayList<Card>());
		player1.getHand().add(new Card(Rank.QUEEN, Suit.HEART));
		player2.setHand(new ArrayList<Card>());
		player2.getHand().add(new Card(Rank.QUEEN, Suit.SPADE));
		player3.setHand(new ArrayList<Card>());
		player3.getHand().add(new Card(Rank.QUEEN, Suit.CLUB));
		player4.setHand(new ArrayList<Card>());
		player4.getHand().add(new Card(Rank.QUEEN, Suit.DIAMOND));

		gameService.playCard(new GameplayRequest(0, "p2", gameId));
		gameService.playCard(new GameplayRequest(0, "p3", gameId));
		gameService.playCard(new GameplayRequest(0, "p4", gameId));
		gameService.playCard(new GameplayRequest(0, "p1", gameId));

		assertThat(player1.getCollectedCards().size()).isEqualTo(0);
		assertThat(player2.getCollectedCards().size()).isEqualTo(32);
		assertThat(player3.getCollectedCards().size()).isEqualTo(0);
		assertThat(player4.getCollectedCards().size()).isEqualTo(0);
		assertThat(player1.getScore()).isEqualTo(320);
		assertThat(player2.getScore()).isEqualTo(0);
		assertThat(player3.getScore()).isEqualTo(160);
		assertThat(player4.getScore()).isEqualTo(160);
	}
}
