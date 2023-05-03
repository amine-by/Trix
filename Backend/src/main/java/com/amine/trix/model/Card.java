package com.amine.trix.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Card {
	private Rank rank;
	private Suit suit;
}
