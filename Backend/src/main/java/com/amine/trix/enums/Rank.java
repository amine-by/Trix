package com.amine.trix.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Rank {
	SEVEN(0,0), EIGHT(1,1), NINE(2,2), TEN(3,6), JACK(4,3), QUEEN(5,4), KING(6,5), ACE(7,7);
	private Integer trixValue;
	private Integer normalValue;
}