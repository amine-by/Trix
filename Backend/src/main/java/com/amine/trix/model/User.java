package com.amine.trix.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.amine.trix.enums.Provider;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mongodb.lang.NonNull;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Document("users")
public class User {
	
	@Id
	private String id;
	
	@NonNull
	private String name;
	
	@JsonIgnore
	private String password;

	@NonNull
	@Indexed(unique = true)
	private String email;

	private String imageUrl;
	
	private Provider provider;
	
	private String providerId;
	
	private String currentGame;
	
}
