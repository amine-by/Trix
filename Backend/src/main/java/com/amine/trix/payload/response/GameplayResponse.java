package com.amine.trix.payload.response;

import java.util.ArrayList;

import com.amine.trix.model.Card;
import com.amine.trix.model.Game;
import com.amine.trix.model.GameStatus;
import com.amine.trix.model.Kingdom;
import com.amine.trix.model.Player;
import com.amine.trix.model.PlayerStatus;
import com.amine.trix.storage.GameStorage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameplayResponse {
	private String gameId;
	private int gameOwner;
	private int turn;
	private GameStatus status;
	private Kingdom currentKingdom;
	private boolean[] trixBoard;
	private ArrayList<Card> normalBoard;
	private Player player;
	private ArrayList<PlayerStatus> otherPlayers;

	public GameplayResponse populateResponse(String gameId, int playerIndex) {
		ArrayList<PlayerStatus> otherPlayers = new ArrayList<PlayerStatus>();
		Game game = GameStorage.getInstance().getGames().get(gameId);
		ArrayList<Player> players = game.getPlayers();
		for (int i = 0; i < players.size(); i++) {
			Player player = players.get(i);
			if (i != playerIndex) {
				PlayerStatus playerStatus = new PlayerStatus();
				playerStatus.setLogin(player.getLogin());
				playerStatus.setScore(player.getScore());
				playerStatus.setHand((player.getHand() == null) ? 0 : player.getHand().size());
				playerStatus.setCollectedCards(
						(player.getCollectedCards() == null) ? 0 : player.getCollectedCards().size());
				otherPlayers.add(playerStatus);
			}
		}

		return new GameplayResponse(game.getGameId(), game.getGameOwner(), game.getTurn(), game.getStatus(),
				game.getCurrentKingdom(), game.getTrixBoard(), game.getNormalBoard(),
				game.getPlayers().get(playerIndex), otherPlayers);
	}
}
