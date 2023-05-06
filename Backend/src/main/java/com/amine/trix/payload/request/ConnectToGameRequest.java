package com.amine.trix.payload.request;

import lombok.Data;

@Data
public class ConnectToGameRequest {
	private String login;
	private String gameId;
}
