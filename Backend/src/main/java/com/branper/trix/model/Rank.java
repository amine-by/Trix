package com.branper.trix.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Rank {
	SEVEN(0), EIGHT(1), NINE(2), TEN(3), JACK(4), QUEEN(5), KING(6), ACE(7);
	private int value;
}