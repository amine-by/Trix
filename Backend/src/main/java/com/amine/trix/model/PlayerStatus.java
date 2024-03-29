package com.amine.trix.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerStatus {
	private String id;
	private String name;
	private int score;
	private int hand;
	private int collectedCards;
}
