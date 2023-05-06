package com.amine.trix.payload.request;

import lombok.Data;

@Data
public class GameplayRequest {
	private int move;
	private String login;
	private String gameId;
}
