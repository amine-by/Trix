package com.amine.trix.model;

import com.amine.trix.enums.Rank;
import com.amine.trix.enums.Suit;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Card {
	private Rank rank;
	private Suit suit;
}
