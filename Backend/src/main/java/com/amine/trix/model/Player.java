package com.amine.trix.model;

import java.util.ArrayList;
import java.util.Arrays;

import lombok.Data;

@Data
public class Player {
	private String id;
	private int score;
	private ArrayList<Card> hand;
	private ArrayList<Card> collectedCards;
	private ArrayList<Kingdom> availableGames;

	public void initializePlayer(String login) {
		this.id = login;
		this.score = 0;
		this.availableGames = new ArrayList<Kingdom>(
				Arrays.asList(Kingdom.KING_OF_HEARTS, Kingdom.QUEENS, Kingdom.DIAMONDS, Kingdom.GENERAL, Kingdom.TRIX));
	}

	public boolean handContainsRank(Rank rank) {
		for (Card card : hand) {
			if (card.getRank() == rank)
				return true;
		}
		return false;
	}

	public boolean suitExistInHand(Suit suit) {
		for (Card card : hand)
			if (card.getSuit() == suit)
				return true;
		return false;
	}

	public int suitNumberInHand(Suit suit) {
		int result = 0;
		for (Card card : hand)
			if (card.getSuit() == suit)
				result++;
		return result;
	}
	
	
}
