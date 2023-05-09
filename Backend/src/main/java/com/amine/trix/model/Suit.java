package com.amine.trix.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Suit {
	HEART(0), SPADE(1), CLUB(2), DIAMOND(3);
	private Integer value;
}
