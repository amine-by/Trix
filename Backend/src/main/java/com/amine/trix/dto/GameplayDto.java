package com.amine.trix.dto;

import java.util.ArrayList;

import com.amine.trix.enums.GameStatus;
import com.amine.trix.enums.Kingdom;
import com.amine.trix.model.Card;
import com.amine.trix.model.Game;
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
		for (int i = 1; i < players.size(); i++) {
			if (game.getGameOwner() == (i + playerIndex) % players.size())
				gameOwner = otherPlayers.size();
			if (game.getTurn() == (i + playerIndex) % players.size())
				turn = otherPlayers.size();
			Player player = players.get((i + playerIndex) % players.size());
			PlayerStatus playerStatus = new PlayerStatus();
			playerStatus.setId(player.getId());
			playerStatus.setName(player.getName());
			playerStatus.setScore(player.getScore());
			playerStatus.setHand((player.getHand() == null) ? 0 : player.getHand().size());
			playerStatus
					.setCollectedCards((player.getCollectedCards() == null) ? 0 : player.getCollectedCards().size());
			otherPlayers.add(playerStatus);
		}

		if (playerIndex == game.getGameOwner())
			gameOwner = 3;
		
		if(playerIndex == game.getTurn())
			turn = 3;

		gameId = game.getId();
		status = game.getStatus();
		currentKingdom = game.getCurrentKingdom();
		trixBoard = game.getTrixBoard();
		normalBoard = game.getNormalBoard();
		player = game.getPlayers().get(playerIndex);
	}
}
