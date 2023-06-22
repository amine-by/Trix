package com.amine.trix.dto;

import java.util.List;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class AvailableGamesDto {
	private List<String> games;
}
