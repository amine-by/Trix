package com.amine.trix.dto;

import java.util.ArrayList;

import com.amine.trix.model.Card;
import com.amine.trix.model.Game;
import com.amine.trix.model.GameStatus;
import com.amine.trix.model.Kingdom;
import com.amine.trix.model.Player;
import com.amine.trix.model.PlayerStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameplayDto {
	private String gameId;
	private int gameOwner;
	private int turn;
	private GameStatus status;
	private Kingdom currentKingdom;
	private boolean[] trixBoard;
	private ArrayList<Card> normalBoard;
	private Player player;
	private ArrayList<PlayerStatus> otherPlayers;

	public void populateResponse(Game game, int playerIndex) {
		otherPlayers = new ArrayList<PlayerStatus>();
		ArrayList<Player> players = game.getPlayers();
		for (int i = 0; i < players.size(); i++) {
			Player player = players.get((i + playerIndex) % players.size());
			if (i != playerIndex) {
				PlayerStatus playerStatus = new PlayerStatus();
				playerStatus.setLogin(player.getId());
				playerStatus.setScore(player.getScore());
				playerStatus.setHand((player.getHand() == null) ? 0 : player.getHand().size());
				playerStatus.setCollectedCards(
						(player.getCollectedCards() == null) ? 0 : player.getCollectedCards().size());
				otherPlayers.add(playerStatus);
			}
		}

		gameId = game.getId();
		gameOwner = game.getGameOwner();
		turn = game.getTurn();
		status = game.getStatus();
		currentKingdom = game.getCurrentKingdom();
		trixBoard = game.getTrixBoard();
		normalBoard = game.getNormalBoard();
		player = game.getPlayers().get(playerIndex);
	}
}
