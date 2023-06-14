package com.amine.trix.model;

import java.util.ArrayList;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.amine.trix.enums.GameStatus;
import com.amine.trix.enums.Kingdom;

import lombok.Data;

@Data
@Document("games")
public class Game {
	@Id
	private String id;
	private ArrayList<Player> players;
	private int gameOwner;
	private int turn;
	private boolean[] trixBoard;
	private ArrayList<Card> normalBoard;
	private GameStatus status;
	private Kingdom currentKingdom;
}
