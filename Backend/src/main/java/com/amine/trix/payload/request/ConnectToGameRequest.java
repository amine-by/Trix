package com.amine.trix.payload.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConnectToGameRequest {
	private String playerId;
	private String gameId;
}
