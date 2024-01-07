package com.amine.trix.model;

import java.util.ArrayList;

import com.amine.trix.enums.Kingdom;

import lombok.Data;

@Data
public class Player {
	private String id;
	private String name;
	private int score;
	private ArrayList<Card> hand;
	private ArrayList<Card> collectedCards;
	private ArrayList<Kingdom> availableGames;
}
